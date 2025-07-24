package com.ignitis.chat.controller;

import com.ignitis.chat.dto.MessageRequest;
import com.ignitis.chat.dto.MessageResponse;
import com.ignitis.chat.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<String> postMessage(
            @Valid @RequestBody MessageRequest messageRequest) {

        messageService.createMessage(messageRequest.getMessage(), messageRequest.getUserId(), messageRequest.getChannelId());

        return ResponseEntity.status(HttpStatus.CREATED).body("Message posted");
    }

    @GetMapping
    public ResponseEntity<List<MessageResponse>> getMessages() {

        List<MessageResponse> messages = messageService.getMessages();

        return ResponseEntity.ok(messages);
    }
}
