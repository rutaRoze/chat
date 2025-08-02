package com.ignitis.chat.controller;

import com.ignitis.chat.dto.UserStatisticResponse;
import com.ignitis.chat.persistance.model.User;
import com.ignitis.chat.service.AdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.ignitis.chat.TestUtil.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    @Test
    void shouldCreateUserSuccessfully_whenAdminHasAccessAndUsernameIsValid() throws Exception {
        Long adminId = 1L;
        User mockUser = buildUser(USER_ID, USERNAME);

        given(adminService.hasAdminAccess(adminId)).willReturn(true);
        given(adminService.createUser(USERNAME)).willReturn(mockUser);

        mockMvc.perform(post("/admin/user")
                        .param("adminId", adminId.toString())
                        .param("username", USERNAME))
                .andExpect(status().isCreated())
                .andExpect(content().string("User username created"));
    }

    @ParameterizedTest
    @CsvSource({
            "1, '', true, 400",
            "1, extremelyLongName, true, 400",
            "2, username, false, 403"
    })
    void createUser_withInvalidUsernameOrWithNoAdminAccess(
            Long adminId, String username, boolean hasAccess, int expectedStatus) throws Exception {

        given(adminService.hasAdminAccess(adminId)).willReturn(hasAccess);

        mockMvc.perform(post("/admin/user")
                        .param("adminId", adminId.toString())
                        .param("username", username))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void shouldDeleteUserSuccessfully_whenAdminHasAccess() throws Exception {
        Long adminId = 1L;

        given(adminService.hasAdminAccess(adminId)).willReturn(true);

        mockMvc.perform(delete("/admin/user/{userId}", USER_ID)
                        .param("adminId", adminId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));
    }

    @ParameterizedTest
    @CsvSource({
            "1, 0, true, 400",
            "2, 1, false, 403"
    })
    void deleteUser_withInvalidUserIdOrWithNoAdminAccess(
            Long adminId, String userIdStr, boolean hasAccess, int expectedStatus) throws Exception {
        given(adminService.hasAdminAccess(adminId)).willReturn(hasAccess);

        mockMvc.perform(delete("/admin/user/{userId}", userIdStr)
                        .param("adminId", adminId.toString()))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void shouldReturnStatistics_whenAdminHasAccess() throws Exception {
        Long adminId = 1L;
        List<UserStatisticResponse> mockStats = List.of(
                buildUserStatisticResponse(USER_ID, USERNAME, 5, MESSAGE)
        );

        given(adminService.hasAdminAccess(adminId)).willReturn(true);
        given(adminService.getUsersStatistic()).willReturn(mockStats);

        mockMvc.perform(get("/admin/user/statistic")
                        .param("adminId", adminId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].username").value(USERNAME))
                .andExpect(jsonPath("$[0].messageCount").value(5))
                .andExpect(jsonPath("$[0].latestMessage").value(MESSAGE));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 400",
            "1, 403"
    })
    void getStatistic_whenAdminHasNoAccess(Long adminId, int expectedStatus) throws Exception {

        given(adminService.hasAdminAccess(adminId)).willReturn(false);

        mockMvc.perform(get("/admin/user/statistic")
                        .param("adminId", adminId.toString()))
                .andExpect(status().is(expectedStatus));
    }
}