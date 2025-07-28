package com.ignitis.chat.mapper;

import com.ignitis.chat.dto.MessageResponse;
import com.ignitis.chat.persistance.model.Message;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

    public MessageResponse mapToMessageResponse(Message message) {

        MessageResponse mappedResponse = MessageResponse.builder()
                .username(message.getSender().getUsername())
                .messageTime(message.getSentAt())
                .message(message.getContent())
                .build();

        return mappedResponse;
    }
}
