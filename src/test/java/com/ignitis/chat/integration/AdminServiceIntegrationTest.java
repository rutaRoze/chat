package com.ignitis.chat.integration;

import com.ignitis.chat.dto.UserStatisticResponse;
import com.ignitis.chat.persistance.ChannelRepository;
import com.ignitis.chat.persistance.MessageRepository;
import com.ignitis.chat.persistance.RoleRepository;
import com.ignitis.chat.persistance.UserRepository;
import com.ignitis.chat.persistance.model.Channel;
import com.ignitis.chat.persistance.model.Role;
import com.ignitis.chat.persistance.model.RoleName;
import com.ignitis.chat.persistance.model.User;
import com.ignitis.chat.service.AdminService;
import com.ignitis.chat.service.MessageService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class AdminServiceIntegrationTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ChannelRepository channelRepository;

    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setup() {
        messageRepository.deleteAll();
        channelRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        userRole = Role.builder().name(RoleName.USER).build();
        adminRole = Role.builder().name(RoleName.ADMIN).build();

        if (roleRepository.count() == 0) {
            roleRepository.saveAll(List.of(userRole, adminRole));
        } else {
            userRole = roleRepository.findRoleByName(RoleName.USER.name()).orElseThrow();
            adminRole = roleRepository.findRoleByName(RoleName.ADMIN.name()).orElseThrow();
        }
    }

    @Test
    void createUser_shouldPersistUserWithUserRole() {
        User created = adminService.createUser("newuser");

        assertEquals("newuser", created.getUsername());
        assertTrue(created.getRoles().stream().anyMatch(r -> r.getName().equals(userRole.getName())));

        Optional<User> persisted = userRepository.findUserById(
                userRepository.findUserIdByUsername("newuser"));

        assertTrue(persisted.isPresent());
        assertEquals("newuser", persisted.get().getUsername());
    }

    @Test
    void createUser_shouldThrowExceptionIfUserExists() {
        adminService.createUser("duplicateUser");

        assertThrows(Exception.class, () -> adminService.createUser("duplicateUser"));
    }

    @Test
    void deleteUser_shouldSoftDeleteUser() {
        adminService.createUser("toDelete");
        Long userId = userRepository.findUserIdByUsername("toDelete");

        adminService.deleteUser(userId);

        Optional<User> deleted = userRepository.findUserById(userId);
        assertTrue(deleted.isPresent());
        assertEquals("anonymous", deleted.get().getUsername());
    }

    @Test
    @Transactional
    void hasAdminAccess_shouldReturnTrueForAdminUser() {
        adminService.createUser("adminUser");
        Long userId = userRepository.findUserIdByUsername("adminUser");

        userRepository.linkUserRole(userId, adminRole.getId());

        assertTrue(adminService.hasAdminAccess(userId));
    }

    @Test
    void hasAdminAccess_shouldReturnFalseForNonAdminUser() {
        adminService.createUser("regularUser");
        Long userId = userRepository.findUserIdByUsername("regularUser");

        assertFalse(adminService.hasAdminAccess(userId));
    }

    @Test
    void hasAdminAccess_shouldThrowIfUserNotFound() {
        assertThrows(EntityNotFoundException.class, () -> adminService.hasAdminAccess(999L));
    }

    @Test
    void getUsersStatistic_shouldReturnMappedResponses() {
        User user = userRepository.save(User.builder().username("statUser").build());
        Channel channel = channelRepository.save(Channel.builder().name("general").build());

        messageService.createMessage("Hello world", user.getId(), channel.getId());

        List<UserStatisticResponse> stats = adminService.getUsersStatistic();

        assertNotNull(stats);
        assertFalse(stats.isEmpty());
        assertEquals("Hello world", stats.get(0).getLatestMessage());
    }
}