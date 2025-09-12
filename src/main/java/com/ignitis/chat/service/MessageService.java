package com.ignitis.chat.service;

import com.ignitis.chat.dto.MessageResponse;
import com.ignitis.chat.exception.UserDeletedException;
import com.ignitis.chat.mapper.MessageMapper;
import com.ignitis.chat.persistance.ChannelRepository;
import com.ignitis.chat.persistance.MessageRepository;
import com.ignitis.chat.persistance.UserRepository;
import com.ignitis.chat.persistance.model.Message;
import com.ignitis.chat.persistance.model.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final MessageMapper messageMapper;

    @Transactional
    public void createMessage(String message, Long userId, Long channelId) {

        User user = userRepository.findUserById(userId).orElseThrow(
                () -> new EntityNotFoundException("User not found by id: " + userId));

        if (user.getDeletedAt() != null) {
            throw new UserDeletedException("Message cannot be posted by anonymous user");
        }

        channelRepository.findChannelById(channelId).orElseThrow(
                () -> new EntityNotFoundException("Channel not found by id: " + channelId));

        LocalDateTime messageSentAt = LocalDateTime.now();

        messageRepository.saveMessage(message, messageSentAt, userId, channelId);
    }

    public List<MessageResponse> getMessages() {
        List<Message> sortedMessages = messageRepository.findAllMessagesSortedBySentAtDesc();

        return sortedMessages.stream()
                .map(messageMapper::mapToMessageResponse)
                .toList();
    }
}
