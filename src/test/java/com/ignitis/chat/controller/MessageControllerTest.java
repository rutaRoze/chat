package com.ignitis.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ignitis.chat.dto.MessageRequest;
import com.ignitis.chat.dto.MessageResponse;
import com.ignitis.chat.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static com.ignitis.chat.TestUtil.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MessageController.class)
@AutoConfigureMockMvc
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MessageService messageService;

    @Test
    void shouldCreateMessage_whenRequestIsValid() throws Exception {
        MessageRequest request = buildMessageRequest(MESSAGE, USER_ID, CHANNEL_ID);

        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().string("Message posted"));

        verify(messageService).createMessage(MESSAGE, USER_ID, CHANNEL_ID);
    }

    @Test
    void shouldReturnBadRequest_whenPayloadIsInvalid() throws Exception {
        MessageRequest invalidRequest = buildMessageRequest("", 1L, 1L);

        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnMessagesList() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        MessageResponse messageResponse1 = buildMessageResponse(MESSAGE, now.minusMinutes(5));
        MessageResponse messageResponse2 = buildMessageResponse(MESSAGE2, now.minusMinutes(20));

        List<MessageResponse> mockMessages = List.of(messageResponse1, messageResponse2);

        given(messageService.getMessages()).willReturn(mockMessages);

        mockMvc.perform(get("/messages"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value(MESSAGE))
                .andExpect(jsonPath("$[1].message").value(MESSAGE2));
    }
}