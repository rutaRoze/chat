package com.ignitis.chat.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class MessageRequest {

    @NotBlank
    private String messageContent;

    @NotNull
    @Min(1)
    private Long userId;

    @NotNull
    @Min(1)
    private Long channelId;
}
