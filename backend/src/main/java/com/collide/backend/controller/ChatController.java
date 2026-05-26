package com.collide.backend.controller;

import com.collide.backend.dto.ChatDto;
import com.collide.backend.dto.MessageDto;
import com.collide.backend.dto.request.ChatRequest;
import com.collide.backend.dto.request.MessageRequest;
import com.collide.backend.model.entity.AppUser;
import com.collide.backend.service.ChatService;
import com.collide.backend.service.CurrentUserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chats")
public class ChatController {
    private final ChatService chatService;
    private final CurrentUserService currentUserService;

    public ChatController(ChatService chatService, CurrentUserService currentUserService) {
        this.chatService = chatService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<ChatDto> list(@RequestParam(required = false) String q,
                              @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        AppUser current = currentUserService.currentUser(userId);
        return chatService.list(current.getId(), q);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChatDto create(@Valid @RequestBody ChatRequest request,
                          @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        AppUser current = currentUserService.currentUser(userId);
        return chatService.createOrGet(current.getId(), request);
    }

    @GetMapping("/{id}/messages")
    public List<MessageDto> messages(@PathVariable UUID id,
                                     @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        AppUser current = currentUserService.currentUser(userId);
        return chatService.messages(id, current.getId());
    }

    @PostMapping("/{id}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageDto send(@PathVariable UUID id, @Valid @RequestBody MessageRequest request,
                           @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        AppUser current = currentUserService.currentUser(userId);
        return chatService.send(id, current.getId(), request);
    }

    @PostMapping("/{id}/read")
    public ChatDto markRead(@PathVariable UUID id,
                            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        AppUser current = currentUserService.currentUser(userId);
        return chatService.markRead(id, current.getId());
    }
}
