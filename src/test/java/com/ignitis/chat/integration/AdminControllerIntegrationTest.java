package com.ignitis.chat.integration;

import com.ignitis.chat.persistance.MessageRepository;
import com.ignitis.chat.persistance.RoleRepository;
import com.ignitis.chat.persistance.UserRepository;
import com.ignitis.chat.persistance.model.Role;
import com.ignitis.chat.persistance.model.RoleName;
import com.ignitis.chat.persistance.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MessageRepository messageRepository;

    private User adminUser;
    private User regularUser;

    @BeforeEach
    void setup() {
        messageRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        roleRepository.save(Role.builder().name(RoleName.USER).build());
        roleRepository.save(Role.builder().name(RoleName.ADMIN).build());

        Role userRole = roleRepository.findRoleByName(RoleName.USER.name()).orElseThrow();
        Role adminRole = roleRepository.findRoleByName(RoleName.ADMIN.name()).orElseThrow();

        adminUser = userRepository.save(
                User.builder().username("admin").roles(Set.of(adminRole)).build()
        );
        regularUser = userRepository.save(
                User.builder().username("user").roles(Set.of(userRole)).build()
        );
    }

    @Test
    void createUser_shouldReturn201ForAdmin() throws Exception {
        mockMvc.perform(post("/admin/user")
                        .param("adminId", adminUser.getId().toString())
                        .param("username", "newuser"))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("User newuser created")));
    }

    @Test
    void createUser_shouldReturn403ForNonAdmin() throws Exception {
        mockMvc.perform(post("/admin/user")
                        .param("adminId", regularUser.getId().toString())
                        .param("username", "newuser"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Access denied")));
    }

    @Test
    void deleteUser_shouldReturn200ForAdmin() throws Exception {
        User toDelete = userRepository.save(User.builder().username("deleteUser").build());

        mockMvc.perform(delete("/admin/user/" + toDelete.getId())
                        .param("adminId", adminUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));
    }

    @Test
    void deleteUser_shouldReturn403ForNonAdmin() throws Exception {
        User toDelete = userRepository.save(User.builder().username("deleteUser").build());

        mockMvc.perform(delete("/admin/user/" + toDelete.getId())
                        .param("adminId", regularUser.getId().toString()))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Access denied")));
    }

    @Test
    void getUsersStatistic_shouldReturn200ForAdmin() throws Exception {
        mockMvc.perform(get("/admin/user/statistic")
                        .param("adminId", adminUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getUsersStatistic_shouldReturn403ForNonAdmin() throws Exception {
        mockMvc.perform(get("/admin/user/statistic")
                        .param("adminId", regularUser.getId().toString()))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Access denied")));
    }

    @Test
    void createUser_shouldReturn400ForInvalidUsername() throws Exception {
        mockMvc.perform(post("/admin/user")
                        .param("adminId", adminUser.getId().toString())
                        .param("username", ""))
                .andExpect(status().isBadRequest());
    }
}