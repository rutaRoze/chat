package com.ignitis.chat.mapper;

import com.ignitis.chat.dto.MessageResponse;
import com.ignitis.chat.persistance.model.Channel;
import com.ignitis.chat.persistance.model.Message;
import com.ignitis.chat.persistance.model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MessageMapper {

    public Message mapToMassage(String message, User user, Channel channel) {

        Message mappedMessage = Message.builder()
                .content(message)
                .sender(user)
                .channel(channel)
                .sentAt(LocalDateTime.now())
                .build();

        return mappedMessage;
    }

    public MessageResponse mapToMessageResponse (Message message) {

        MessageResponse mappedResponse = MessageResponse.builder()
                .username(message.getSender().getUsername())
                .messageTime(message.getSentAt())
                .message(message.getContent())
                .build();

        return mappedResponse;
    }
}
