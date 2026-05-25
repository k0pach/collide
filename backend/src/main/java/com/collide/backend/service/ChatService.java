package com.collide.backend.service;

import com.collide.backend.dto.ChatDto;
import com.collide.backend.dto.MessageDto;
import com.collide.backend.dto.request.ChatRequest;
import com.collide.backend.dto.request.MessageRequest;
import com.collide.backend.exception.BadRequestException;
import com.collide.backend.exception.ForbiddenException;
import com.collide.backend.exception.NotFoundException;
import com.collide.backend.model.entity.*;
import com.collide.backend.repository.ChatRepository;
import com.collide.backend.repository.MessageRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserService userService;
    private final DtoMapper mapper;

    public ChatService(ChatRepository chatRepository, MessageRepository messageRepository, UserService userService, DtoMapper mapper) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.userService = userService;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<ChatDto> list(UUID currentUserId, String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        return chatRepository.findForUser(currentUserId).stream()
                .map(chat -> toChatDto(chat, currentUserId))
                .filter(dto -> q.isBlank() || dto.name().toLowerCase().contains(q) || dto.handle().toLowerCase().contains(q))
                .toList();
    }

    @Transactional
    public ChatDto createOrGet(UUID currentUserId, ChatRequest request) {
        if (currentUserId.equals(request.companionId())) throw new BadRequestException("Нельзя создать чат с самим собой");
        AppUser current = userService.find(currentUserId);
        AppUser companion = userService.find(request.companionId());
        Chat chat = chatRepository.findPrivateChat(currentUserId, request.companionId()).orElseGet(() -> {
            Chat created = new Chat();
            created.setFirstUser(current);
            created.setSecondUser(companion);
            return chatRepository.save(created);
        });
        return toChatDto(chat, currentUserId);
    }

    @Transactional(readOnly = true)
    public List<MessageDto> messages(UUID chatId, UUID currentUserId) {
        Chat chat = find(chatId);
        ensureParticipant(chat, currentUserId);
        return messageRepository.findByChatIdAndDeletedFalseOrderByCreatedAtAsc(chatId).stream()
                .map(message -> mapper.message(message, currentUserId))
                .toList();
    }

    @Transactional
    public MessageDto send(UUID chatId, UUID currentUserId, MessageRequest request) {
        Chat chat = find(chatId);
        ensureParticipant(chat, currentUserId);
        Message message = new Message();
        message.setChat(chat);
        message.setSender(userService.find(currentUserId));
        message.setBody(request.body().trim());
        chat.setUpdatedAt(OffsetDateTime.now());
        chatRepository.save(chat);
        return mapper.message(messageRepository.save(message), currentUserId);
    }

    @Transactional
    public ChatDto markRead(UUID chatId, UUID currentUserId) {
        Chat chat = find(chatId);
        ensureParticipant(chat, currentUserId);
        OffsetDateTime now = OffsetDateTime.now();
        if (chat.getFirstUser().getId().equals(currentUserId)) chat.setFirstUserLastReadAt(now);
        else chat.setSecondUserLastReadAt(now);
        chatRepository.save(chat);
        return toChatDto(chat, currentUserId);
    }

    private ChatDto toChatDto(Chat chat, UUID currentUserId) {
        AppUser companion = chat.getFirstUser().getId().equals(currentUserId) ? chat.getSecondUser() : chat.getFirstUser();
        Message last = messageRepository.findTopByChatIdAndDeletedFalseOrderByCreatedAtDesc(chat.getId()).orElse(null);
        String preview = last == null ? "Нет сообщений" : last.getBody();
        return new ChatDto(chat.getId(), companion.getId(), companion.getDisplayName(), "@" + companion.getUsername(),
                mapper.avatarTone(companion.getId()), preview, unread(chat, currentUserId), chat.getUpdatedAt());
    }

    private int unread(Chat chat, UUID currentUserId) {
        OffsetDateTime readAt = chat.getFirstUser().getId().equals(currentUserId) ? chat.getFirstUserLastReadAt() : chat.getSecondUserLastReadAt();
        long value = readAt == null
                ? messageRepository.countByChat_IdAndDeletedFalseAndSender_IdNot(chat.getId(), currentUserId)
                : messageRepository.countByChat_IdAndDeletedFalseAndCreatedAtAfterAndSender_IdNot(chat.getId(), readAt, currentUserId);
        return (int) Math.min(value, Integer.MAX_VALUE);
    }

    private Chat find(UUID chatId) {
        return chatRepository.findById(chatId).orElseThrow(() -> new NotFoundException("Чат не найден"));
    }

    private void ensureParticipant(Chat chat, UUID userId) {
        if (!chat.getFirstUser().getId().equals(userId) && !chat.getSecondUser().getId().equals(userId)) {
            throw new ForbiddenException("Пользователь не является участником чата");
        }
    }
}
