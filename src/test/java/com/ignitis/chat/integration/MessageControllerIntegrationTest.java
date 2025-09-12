package com.ignitis.chat.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ignitis.chat.dto.MessageRequest;
import com.ignitis.chat.persistance.ChannelRepository;
import com.ignitis.chat.persistance.MessageRepository;
import com.ignitis.chat.persistance.UserRepository;
import com.ignitis.chat.persistance.model.Channel;
import com.ignitis.chat.persistance.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MessageControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User activeUser;
    private User deletedUser;
    private Channel testChannel;

    @BeforeEach
    void setup() {
        messageRepository.deleteAll();
        userRepository.deleteAll();
        channelRepository.deleteAll();

        activeUser = User.builder().username("active").build();
        deletedUser = User.builder().username("anonymous").deletedAt(LocalDateTime.now()).build();
        testChannel = Channel.builder().name("general").build();

        userRepository.saveAll(List.of(activeUser, deletedUser));
        channelRepository.save(testChannel);
    }

    @Test
    void postMessage_shouldReturn201ForValidRequest() throws Exception {
        MessageRequest request = MessageRequest.builder()
                .messageContent("Hello world")
                .userId(activeUser.getId())
                .channelId(testChannel.getId())
                .build();

        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Message posted"));
    }

    @Test
    void postMessage_shouldReturn404ForInvalidUser() throws Exception {
        MessageRequest request = MessageRequest.builder()
                .messageContent("Hello world")
                .userId(999L)
                .channelId(testChannel.getId())
                .build();

        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void postMessage_shouldReturn404ForInvalidChannel() throws Exception {
        MessageRequest request = MessageRequest.builder()
                .messageContent("Hello world")
                .userId(activeUser.getId())
                .channelId(999L)
                .build();

        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void postMessage_shouldReturn409ForDeletedUser() throws Exception {
        MessageRequest request = MessageRequest.builder()
                .messageContent("Hello world")
                .userId(deletedUser.getId())
                .channelId(testChannel.getId())
                .build();

        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("anonymous user")));
    }

    @Test
    void getMessages_shouldReturnAllMessages() throws Exception {
        messageRepository.saveMessage("First", LocalDateTime.now().minusMinutes(1), activeUser.getId(), testChannel.getId());
        messageRepository.saveMessage("Second", LocalDateTime.now(), activeUser.getId(), testChannel.getId());

        mockMvc.perform(get("/messages"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].message").value("Second"))
                .andExpect(jsonPath("$[1].message").value("First"));
    }
}