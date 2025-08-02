package com.ignitis.chat;

import com.ignitis.chat.dto.MessageRequest;
import com.ignitis.chat.dto.MessageResponse;
import com.ignitis.chat.dto.UserStatisticResponse;
import com.ignitis.chat.persistance.model.*;

import java.time.LocalDateTime;

public class TestUtil {

    public static final String USERNAME = "username";
    public static final String USERNAME2 = "username2";
    public static final String USER_ROLE = RoleName.USER.name();
    public static final String MESSAGE = "message";
    public static final String MESSAGE2 = "message2";
    public static final Long USER_ID = 1L;
    public static final Long CHANNEL_ID = 1L;

    public static User buildUser(Long id, String username) {
        return User.builder()
                .id(id)
                .username(username)
                .build();
    }

    public static User buildUser(Long id, String username, LocalDateTime deletionTime) {
        return User.builder()
                .id(id)
                .username(username)
                .deletedAt(deletionTime)
                .build();
    }

    public static Role buildRole() {
        return Role.builder()
                .id(1L)
                .name(RoleName.USER)
                .build();
    }

    public static UserStatisticResponse buildUserStatisticResponse(Long id, String username, int messageCount, String message) {
        return UserStatisticResponse.builder()
                .userId(id)
                .username(username)
                .messageCount(messageCount)
                .latestMessage(message)
                .build();
    }

    public static Channel buildChannel() {
        return Channel.builder()
                .id(1L)
                .name("public")
                .build();
    }

    public static Message buildMessage(Long id, String messageContent, LocalDateTime sentAt) {
        return Message.builder()
                .id(id)
                .content(messageContent)
                .sentAt(sentAt)
                .build();
    }

    public static MessageResponse buildMessageResponse(String messageContent, LocalDateTime sentAt) {
        return MessageResponse.builder()
                .message(messageContent)
                .messageTime(sentAt)
                .build();
    }

    public static MessageRequest buildMessageRequest(String messageContent, Long userId, Long channelId) {
        return MessageRequest.builder()
                .messageContent(messageContent)
                .userId(userId)
                .channelId(channelId)
                .build();
    }
}

