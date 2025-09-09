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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.ignitis.chat.TestUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ChannelRepository channelRepository;
    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private MessageService messageService;

    @Test
    void createMessage_shouldSaveMessageSuccessfully() {
        User mockUser = buildUser(USER_ID, USERNAME);
        Channel mockChannel = buildChannel();

        when(userRepository.findUserById(mockUser.getId())).thenReturn(Optional.of(mockUser));
        when(channelRepository.findChannelById(mockChannel.getId())).thenReturn(Optional.of(mockChannel));

        messageService.createMessage(MESSAGE, mockUser.getId(), mockChannel.getId());

        verify(messageRepository).saveMessage(eq(MESSAGE), any(LocalDateTime.class), eq(mockUser.getId()), eq(mockChannel.getId()));
    }

    @Test
    void createMessage_shouldThrowWhenUserNotFound() {
        when(userRepository.findUserById(USER_ID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                messageService.createMessage(MESSAGE, USER_ID, CHANNEL_ID));

        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void createMessage_shouldThrowWhenUserIsDeleted() {
        User mockUser = buildUser(USER_ID, USERNAME, LocalDateTime.now());

        when(userRepository.findUserById(mockUser.getId())).thenReturn(Optional.of(mockUser));

        UserDeletedException exception = assertThrows(UserDeletedException.class, () ->
                messageService.createMessage(MESSAGE, USER_ID, CHANNEL_ID));

        assertTrue(exception.getMessage().contains("cannot be posted by anonymous user"));
    }

    @Test
    void createMessage_shouldThrowWhenChannelNotFound() {
        User mockUser = buildUser(USER_ID, USERNAME);

        when(userRepository.findUserById(USER_ID)).thenReturn(Optional.of(mockUser));
        when(channelRepository.findChannelById(CHANNEL_ID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                messageService.createMessage(MESSAGE, USER_ID, CHANNEL_ID));

        assertTrue(exception.getMessage().contains("Channel not found"));
    }

    @Test
    void getMessages_shouldReturnMappedResponsesInDescendingOrder() {
        LocalDateTime messageTime = LocalDateTime.now();

        Message message1 = buildMessage(1L, MESSAGE, messageTime.minusMinutes(200));
        Message message2 = buildMessage(2L, MESSAGE2, messageTime);

        MessageResponse response1 = buildMessageResponse(MESSAGE, messageTime.minusMinutes(200));
        MessageResponse response2 = buildMessageResponse(MESSAGE2, messageTime);

        List<Message> messages = List.of(message2, message1);

        when(messageRepository.findAllMessagesSortedBySentAtDesc()).thenReturn(messages);
        when(messageMapper.mapToMessageResponse(message2)).thenReturn(response2);
        when(messageMapper.mapToMessageResponse(message1)).thenReturn(response1);

        List<MessageResponse> result = messageService.getMessages();

        assertEquals(2, result.size());
        assertEquals(MESSAGE2, result.get(0).getMessage());
        assertEquals(MESSAGE, result.get(1).getMessage());
        assertTrue(result.get(0).getMessageTime().isAfter(result.get(1).getMessageTime()));
    }
}