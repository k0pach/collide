package com.collide.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.collide.backend.dto.ChatDto;
import com.collide.backend.dto.MessageDto;
import com.collide.backend.dto.request.ChatRequest;
import com.collide.backend.dto.request.MessageRequest;
import com.collide.backend.exception.BadRequestException;
import com.collide.backend.exception.ForbiddenException;
import com.collide.backend.exception.NotFoundException;
import com.collide.backend.model.entity.AppUser;
import com.collide.backend.model.entity.Chat;
import com.collide.backend.model.entity.Message;
import com.collide.backend.model.enums.UserRole;
import com.collide.backend.repository.ChatRepository;
import com.collide.backend.repository.MessageRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRepository chatRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private UserService userService;
    @Mock
    private DtoMapper mapper;

    private ChatService service;

    @BeforeEach
    void setUp() {
        service = new ChatService(chatRepository, messageRepository, userService, mapper);
    }

    @Test
    void listBuildsDtosAndFiltersByQuery() {
        UUID currentUserId = UUID.randomUUID();
        AppUser current = user(currentUserId, "me", "Me");
        AppUser john = user(UUID.randomUUID(), "john", "John");
        AppUser anna = user(UUID.randomUUID(), "anna", "Anna");
        Chat chat1 = chat(UUID.randomUUID(), current, john);
        Chat chat2 = chat(UUID.randomUUID(), current, anna);

        when(chatRepository.findForUser(currentUserId)).thenReturn(List.of(chat1, chat2));
        when(mapper.avatarTone(john.getId())).thenReturn("orange");
        when(mapper.avatarTone(anna.getId())).thenReturn("red");
        when(messageRepository.findTopByChatIdAndDeletedFalseOrderByCreatedAtDesc(chat1.getId())).thenReturn(Optional.of(message(chat1, john, "Hello")));
        when(messageRepository.findTopByChatIdAndDeletedFalseOrderByCreatedAtDesc(chat2.getId())).thenReturn(Optional.empty());
        when(messageRepository.countByChat_IdAndDeletedFalseAndSender_IdNot(chat1.getId(), currentUserId)).thenReturn(2L);
        when(messageRepository.countByChat_IdAndDeletedFalseAndSender_IdNot(chat2.getId(), currentUserId)).thenReturn(1L);

        List<ChatDto> filtered = service.list(currentUserId, "@john");

        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0).name()).isEqualTo("John");
        assertThat(filtered.get(0).preview()).isEqualTo("Hello");
        assertThat(filtered.get(0).unread()).isEqualTo(2);

        List<ChatDto> all = service.list(currentUserId, null);
        assertThat(all).hasSize(2);

        AppUser nemo = user(UUID.randomUUID(), "nemo_user", "Captain Nemo");
        Chat chat3 = chat(UUID.randomUUID(), current, nemo);
        when(chatRepository.findForUser(currentUserId)).thenReturn(List.of(chat1, chat2, chat3));
        when(mapper.avatarTone(nemo.getId())).thenReturn("brown");
        when(messageRepository.findTopByChatIdAndDeletedFalseOrderByCreatedAtDesc(chat3.getId())).thenReturn(Optional.empty());
        when(messageRepository.countByChat_IdAndDeletedFalseAndSender_IdNot(chat3.getId(), currentUserId)).thenReturn(0L);

        List<ChatDto> byName = service.list(currentUserId, "captain");
        assertThat(byName).hasSize(1);
        assertThat(byName.get(0).name()).isEqualTo("Captain Nemo");
    }

    @Test
    void createOrGetThrowsForSelfChat() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> service.createOrGet(id, new ChatRequest(id)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Нельзя создать чат с самим собой");
    }

    @Test
    void createOrGetCreatesChatWhenMissing() {
        UUID currentUserId = UUID.randomUUID();
        UUID companionId = UUID.randomUUID();
        AppUser current = user(currentUserId, "me", "Me");
        AppUser companion = user(companionId, "john", "John");

        when(userService.find(currentUserId)).thenReturn(current);
        when(userService.find(companionId)).thenReturn(companion);
        when(chatRepository.findPrivateChat(currentUserId, companionId)).thenReturn(Optional.empty());
        when(chatRepository.save(any(Chat.class))).thenAnswer(invocation -> {
            Chat chat = invocation.getArgument(0);
            chat.setId(UUID.randomUUID());
            return chat;
        });
        when(mapper.avatarTone(companionId)).thenReturn("orange");
        when(messageRepository.findTopByChatIdAndDeletedFalseOrderByCreatedAtDesc(any(UUID.class))).thenReturn(Optional.empty());
        when(messageRepository.countByChat_IdAndDeletedFalseAndSender_IdNot(any(UUID.class), any(UUID.class))).thenReturn(0L);

        ChatDto dto = service.createOrGet(currentUserId, new ChatRequest(companionId));

        assertThat(dto.companionId()).isEqualTo(companionId);
        assertThat(dto.preview()).isEqualTo("Нет сообщений");
    }

    @Test
    void messagesThrowsWhenUserNotParticipant() {
        UUID chatId = UUID.randomUUID();
        Chat chat = chat(chatId, user(UUID.randomUUID(), "a", "A"), user(UUID.randomUUID(), "b", "B"));
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));

        assertThatThrownBy(() -> service.messages(chatId, UUID.randomUUID()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Пользователь не является участником чата");
    }

    @Test
    void messagesReturnsMappedDtosForParticipant() {
        UUID currentUserId = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();
        AppUser current = user(currentUserId, "me", "Me");
        AppUser companion = user(UUID.randomUUID(), "john", "John");
        Chat chat = chat(chatId, current, companion);
        Message m1 = message(chat, current, "first");
        Message m2 = message(chat, companion, "second");
        MessageDto d1 = new MessageDto(UUID.randomUUID(), chatId, currentUserId, "Me", "first", true, OffsetDateTime.now());
        MessageDto d2 = new MessageDto(UUID.randomUUID(), chatId, companion.getId(), "John", "second", false, OffsetDateTime.now());

        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(messageRepository.findByChatIdAndDeletedFalseOrderByCreatedAtAsc(chatId)).thenReturn(List.of(m1, m2));
        when(mapper.message(m1, currentUserId)).thenReturn(d1);
        when(mapper.message(m2, currentUserId)).thenReturn(d2);

        assertThat(service.messages(chatId, currentUserId)).containsExactly(d1, d2);
    }

    @Test
    void sendSavesMessageAndUpdatesChatTimestamp() {
        UUID currentUserId = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();
        AppUser current = user(currentUserId, "me", "Me");
        AppUser companion = user(UUID.randomUUID(), "john", "John");
        Chat chat = chat(chatId, current, companion);
        MessageDto dto = new MessageDto(UUID.randomUUID(), chatId, currentUserId, "Me", "Hi", true, OffsetDateTime.now());

        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(userService.find(currentUserId)).thenReturn(current);
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.message(any(Message.class), any(UUID.class))).thenReturn(dto);

        MessageDto result = service.send(chatId, currentUserId, new MessageRequest("  Hi  "));

        ArgumentCaptor<Message> msgCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(msgCaptor.capture());
        assertThat(msgCaptor.getValue().getBody()).isEqualTo("Hi");
        assertThat(chat.getUpdatedAt()).isNotNull();
        assertThat(result).isSameAs(dto);
    }

    @Test
    void markReadUpdatesFirstOrSecondReaderAndUsesUnreadAfterTimestamp() {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        Chat chat = chat(UUID.randomUUID(), user(firstId, "first", "First"), user(secondId, "second", "Second"));
        chat.setUpdatedAt(OffsetDateTime.now());

        when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));
        when(mapper.avatarTone(secondId)).thenReturn("orange");
        when(messageRepository.findTopByChatIdAndDeletedFalseOrderByCreatedAtDesc(chat.getId())).thenReturn(Optional.empty());
        when(messageRepository.countByChat_IdAndDeletedFalseAndCreatedAtAfterAndSender_IdNot(eq(chat.getId()), any(OffsetDateTime.class), any(UUID.class)))
                .thenReturn(3L);

        ChatDto firstReadDto = service.markRead(chat.getId(), firstId);
        assertThat(chat.getFirstUserLastReadAt()).isNotNull();
        assertThat(firstReadDto.unread()).isEqualTo(3);

        when(mapper.avatarTone(firstId)).thenReturn("red");
        when(messageRepository.countByChat_IdAndDeletedFalseAndCreatedAtAfterAndSender_IdNot(eq(chat.getId()), any(OffsetDateTime.class), any(UUID.class)))
                .thenReturn(1L);

        ChatDto secondReadDto = service.markRead(chat.getId(), secondId);
        assertThat(chat.getSecondUserLastReadAt()).isNotNull();
        assertThat(secondReadDto.companionId()).isEqualTo(firstId);
    }

    @Test
    void operationsThrowNotFoundWhenChatMissing() {
        UUID chatId = UUID.randomUUID();
        when(chatRepository.findById(chatId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.messages(chatId, UUID.randomUUID()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Чат не найден");
    }

    private AppUser user(UUID id, String username, String displayName) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        user.setDisplayName(displayName);
        user.setEmail(username + "@mail.test");
        user.setPasswordHash("hash");
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        return user;
    }

    private Chat chat(UUID id, AppUser first, AppUser second) {
        Chat chat = new Chat();
        chat.setId(id);
        chat.setFirstUser(first);
        chat.setSecondUser(second);
        chat.setUpdatedAt(OffsetDateTime.now());
        return chat;
    }

    private Message message(Chat chat, AppUser sender, String body) {
        Message message = new Message();
        message.setId(UUID.randomUUID());
        message.setChat(chat);
        message.setSender(sender);
        message.setBody(body);
        message.setCreatedAt(OffsetDateTime.now());
        return message;
    }
}
