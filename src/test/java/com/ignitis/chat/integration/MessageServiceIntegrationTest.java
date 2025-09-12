package com.ignitis.chat.integration;

import com.ignitis.chat.dto.MessageResponse;
import com.ignitis.chat.exception.UserDeletedException;
import com.ignitis.chat.mapper.MessageMapper;
import com.ignitis.chat.persistance.ChannelRepository;
import com.ignitis.chat.persistance.MessageRepository;
import com.ignitis.chat.persistance.UserRepository;
import com.ignitis.chat.persistance.model.Channel;
import com.ignitis.chat.persistance.model.Message;
import com.ignitis.chat.persistance.model.User;
import com.ignitis.chat.service.MessageService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class MessageServiceIntegrationTest {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageMapper messageMapper;

    private User activeUser;
    private User deletedUser;
    private Channel testChannel;

    @BeforeEach
    void setup() {
        messageRepository.deleteAll();
        channelRepository.deleteAll();

        activeUser = User.builder()
                .username("activeUser")
                .build();

        deletedUser = User.builder()
                .username("anonymous")
                .deletedAt(LocalDateTime.now())
                .build();

        testChannel = Channel.builder()
                .name("general")
                .build();

        userRepository.saveAll(List.of(activeUser, deletedUser));
        channelRepository.save(testChannel);
    }

    @Test
    void createMessage_shouldPersistMessage() {
        messageService.createMessage("Hello world", activeUser.getId(), testChannel.getId());

        List<Message> messages = messageRepository.findAllMessagesSortedBySentAtDesc();
        assertEquals(1, messages.size());
        assertEquals("Hello world", messages.get(0).getContent());
        assertEquals(activeUser.getId(), messages.get(0).getSender().getId());
        assertEquals(testChannel.getId(), messages.get(0).getChannel().getId());
    }

    @Test
    void createMessage_shouldThrowIfUserNotFound() {
        Long invalidUserId = 999L;
        Long testChannelId = testChannel.getId();
        assertThrows(EntityNotFoundException.class, () ->
                messageService.createMessage("Test", invalidUserId, testChannelId));
    }

    @Test
    void createMessage_shouldThrowIfChannelNotFound() {
        Long invalidChannelId = 999L;
        Long activeUserId = activeUser.getId();
        assertThrows(EntityNotFoundException.class, () ->
                messageService.createMessage("Test", activeUserId, invalidChannelId));
    }

    @Test
    void createMessage_shouldThrowIfUserIsDeleted() {
        Long deletedUserId = deletedUser.getId();
        Long testChannelId = testChannel.getId();
        assertThrows(UserDeletedException.class, () ->
                messageService.createMessage("Test", deletedUserId, testChannelId));
    }

    @Test
    void getMessages_shouldReturnMappedResponses() {
        messageService.createMessage("First", activeUser.getId(), testChannel.getId());
        messageService.createMessage("Second", activeUser.getId(), testChannel.getId());

        List<MessageResponse> responses = messageService.getMessages();

        assertEquals(2, responses.size());
        assertEquals("Second", responses.get(0).getMessage());
        assertEquals("First", responses.get(1).getMessage());
    }
}