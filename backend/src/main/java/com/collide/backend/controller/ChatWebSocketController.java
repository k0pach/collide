package com.collide.backend.controller;

import com.collide.backend.dto.MessageDto;
import com.collide.backend.dto.request.MessageRequest;
import com.collide.backend.model.entity.AppUser;
import com.collide.backend.security.JwtService;
import com.collide.backend.service.ChatService;
import com.collide.backend.service.CurrentUserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {
    private final ChatService chatService;
    private final CurrentUserService currentUserService;
    private final JwtService jwtService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(ChatService chatService, CurrentUserService currentUserService,
                                   JwtService jwtService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.currentUserService = currentUserService;
        this.jwtService = jwtService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chats/{chatId}/send")
    public void send(@DestinationVariable UUID chatId,
                     @Valid @Payload MessageRequest request,
                     SimpMessageHeaderAccessor headers) {
        UUID currentUserId = resolveUserId(headers);
        MessageDto message = chatService.send(chatId, currentUserId, request);
        messagingTemplate.convertAndSend("/topic/chats/" + chatId, message);
    }

    private UUID resolveUserId(SimpMessageHeaderAccessor headers) {
        String authorization = firstHeader(headers, "Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return jwtService.extractUserId(authorization.substring(7));
        }
        String userHeader = firstHeader(headers, "X-User-Id");
        UUID headerUserId = userHeader == null || userHeader.isBlank() ? null : UUID.fromString(userHeader);
        AppUser current = currentUserService.currentUser(headerUserId);
        return current.getId();
    }

    private String firstHeader(SimpMessageHeaderAccessor headers, String name) {
        List<String> values = headers.getNativeHeader(name);
        return values == null || values.isEmpty() ? null : values.get(0);
    }
}
