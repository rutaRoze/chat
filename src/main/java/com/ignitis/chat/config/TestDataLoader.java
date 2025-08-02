package com.ignitis.chat.config;

import com.ignitis.chat.persistance.ChannelRepository;
import com.ignitis.chat.persistance.MessageRepository;
import com.ignitis.chat.persistance.RoleRepository;
import com.ignitis.chat.persistance.UserRepository;
import com.ignitis.chat.persistance.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TestDataLoader implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;

    private Role adminRole;
    private Role userRole;
    private Channel channel;
    private User admin;
    private User user1;
    private User user2;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        persistRoles();
        persistChannel();
        createAdmin();
        createUser1();
        createUser2();
        createMessageForAdmin();
        createMessageForUser1();
        createMessageForUser2();
    }

    private void persistRoles() {
        adminRole = createAdminRole();
        userRole = createUserRole();

        roleRepository.save(adminRole);
        roleRepository.save(userRole);
    }

    private Role createAdminRole() {
        return Role.builder()
                .name(RoleName.ADMIN)
                .build();
    }

    private Role createUserRole() {
        return Role.builder()
                .name(RoleName.USER)
                .build();
    }

    private void createAdmin() {
        admin = persistUser("administrator", adminRole);
    }

    private void createUser1() {
        user1 = persistUser("user1", userRole);
    }

    private void createUser2() {
        user2 = persistUser("user2", userRole);
    }

    private User persistUser(String username, Role role) {

        User user = User.builder()
                .username(username)
                .roles(Set.of(role))
                .build();

        return userRepository.save(user);
    }

    private Channel createChannel() {

        return Channel.builder()
                .name("public room")
                .build();
    }

    private void persistChannel() {
        channel = createChannel();
        channelRepository.save(channel);
    }

    private void createMessageForAdmin() {
        persistMessage("Admin has nothing to say.", LocalDateTime.now().minusMinutes(5), admin);
    }

    private void createMessageForUser1() {
        persistMessage("Hello and greeting from User1!", LocalDateTime.now().minusMinutes(25), user1);
    }

    private void createMessageForUser2() {
        persistMessage("User2 says hello from the other side!", LocalDateTime.now().minusMinutes(160), user2);
    }

    private void persistMessage(String content, LocalDateTime sentAt, User user) {

        Message message = Message.builder()
                .content(content)
                .sentAt(sentAt)
                .sender(user)
                .channel(channel)
                .build();

        messageRepository.save(message);
    }
}
