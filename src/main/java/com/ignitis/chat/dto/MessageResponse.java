package com.ignitis.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MessageResponse {

    private String username;

    @Schema(description = "Timestamp then message was sent", example = "2025-01-01T08:00:00")
    private LocalDateTime messageTime;

    private String message;
}
