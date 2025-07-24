package com.ignitis.chat.service;

import com.ignitis.chat.dto.MessageResponse;
import com.ignitis.chat.exception.UserDeletedException;
import com.ignitis.chat.mapper.MessageMapper;
import com.ignitis.chat.persistance.ChannelRepository;
import com.ignitis.chat.persistance.MessageRepository;
import com.ignitis.chat.persistance.UserRepository;
import com.ignitis.chat.persistance.model.Channel;
import com.ignitis.chat.persistance.model.Message;
import com.ignitis.chat.persistance.model.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final MessageMapper messageMapper;

    public void createMessage(String message, Long userId, Long channelId) {

        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("User not found by id: " + userId));

        if (user.getDeletedAt() != null) {
            throw new UserDeletedException("Message cannot be posted by anonymous user");
        }

        Channel channel = channelRepository.findById(channelId).orElseThrow(
                () -> new EntityNotFoundException("Channel not found by id: " + channelId));

        Message messageToSave = messageMapper.mapToMassage(message, user, channel);

        user.addMessage(messageToSave);
        channel.addMessage(messageToSave);

        messageRepository.save(messageToSave);
    }

    public List<MessageResponse> getMessages() {
        List<Message> messages = messageRepository.findAll();

        List<MessageResponse> sortedMessageResponses = messages.stream()
                .map(messageMapper::mapToMessageResponse)
                .sorted(Comparator.comparing(MessageResponse::getMessageTime).reversed())
                .toList();

        return sortedMessageResponses;
    }
}
