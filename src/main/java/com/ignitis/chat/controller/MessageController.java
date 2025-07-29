package com.ignitis.chat.controller;

import com.ignitis.chat.dto.MessageRequest;
import com.ignitis.chat.dto.MessageResponse;
import com.ignitis.chat.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Message management", description = "Post message and get message list")
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "Post new message")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Message successfully posted"),
            @ApiResponse(responseCode = "400", description = "Invalid message"),
            @ApiResponse(responseCode = "404", description = "User or channel not found"),
            @ApiResponse(responseCode = "409", description = "Message posted by anonymous user")})
    @PostMapping
    public ResponseEntity<String> postMessage(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Post new message", required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageRequest.class),
                            examples = @ExampleObject(value =
                                    "{ \"messageContent\": \"New Message\", \"userId\": \"1\", \"channelId\": \"1\" }")))
            @Valid @RequestBody MessageRequest messageRequest) {

        messageService.createMessage(messageRequest.getMessageContent(),
                messageRequest.getUserId(), messageRequest.getChannelId());

        return ResponseEntity.status(HttpStatus.CREATED).body("Message posted");
    }

    @Operation(summary = "Retrieve all messages")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns a list of messages",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = MessageResponse.class))))})
    @GetMapping
    public ResponseEntity<List<MessageResponse>> getMessages() {

        List<MessageResponse> messages = messageService.getMessages();

        return ResponseEntity.ok(messages);
    }
}
