package com.ignitis.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserStatisticResponse {

    private Long userId;
    private String username;
    private int messageCount;

    @Schema(description = "Timestamp of the oldest message", example = "2025-01-01T08:00:00")
    private LocalDateTime oldestMessageTime;

    @Schema(description = "Timestamp of the latest message", example = "2025-01-01T08:00:00")

    private LocalDateTime latestMessageTime;
    private int averageMessageLength;
    private String latestMessage;
}
