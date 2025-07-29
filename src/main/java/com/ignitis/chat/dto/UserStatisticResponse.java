package com.ignitis.chat.dto;

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
    private LocalDateTime oldestMessageTime;
    private LocalDateTime latestMessageTime;
    private int averageMessageLength;
    private String latestMessage;
}
