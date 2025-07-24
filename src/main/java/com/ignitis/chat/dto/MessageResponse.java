package com.ignitis.chat.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MessageResponse {

    private String username;
    private LocalDateTime messageTime;
    private String message;
}
