package com.ignitis.chat.service;

import com.ignitis.chat.dto.UserStatisticResponse;
import com.ignitis.chat.exception.UserAlreadyExistsException;
import com.ignitis.chat.mapper.UserStatisticMapper;
import com.ignitis.chat.persistance.MessageRepository;
import com.ignitis.chat.persistance.RoleRepository;
import com.ignitis.chat.persistance.UserRepository;
import com.ignitis.chat.persistance.model.Role;
import com.ignitis.chat.persistance.model.RoleName;
import com.ignitis.chat.persistance.model.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MessageRepository messageRepository;
    private final UserStatisticMapper userStatisticMapper;

    @Transactional
    public User createUser(String username) {

        String trimmedUsername = username.trim();

        if (userRepository.doesUserExistIgnoreCase(trimmedUsername)) {
            throw new UserAlreadyExistsException(String.format("User %s already exists", trimmedUsername));
        }

        Role userRole = roleRepository.findRoleByName(RoleName.USER.name())
                .orElseThrow(() -> new EntityNotFoundException("Requested role not found"));

        userRepository.saveUser(trimmedUsername);

        Long userId = userRepository.findUserIdByUsername(trimmedUsername);

        userRepository.linkUserRole(userId, userRole.getId());

        return User.builder()
                .username(trimmedUsername)
                .roles(Set.of(userRole))
                .build();
    }

    @Transactional
    public void deleteUser(Long userId) {
        findUser(userId);

        String softDeletedUsername = "anonymous";
        LocalDateTime deletedAt = LocalDateTime.now();

        userRepository.softDeleteUser(userId, softDeletedUsername, deletedAt);
    }

    public List<UserStatisticResponse> getUsersStatistic() {

        List<Object[]> rawUserStatisticsData = messageRepository.findUserStatisticsRaw();
        List<UserStatisticResponse> responses = new ArrayList<>();

        for (Object[] oneUserData : rawUserStatisticsData) {

            UserStatisticResponse response = userStatisticMapper.mapToUseStatisticResponse(oneUserData);

            responses.add(response);
        }

        return responses;
    }

    @Transactional
    public boolean hasAdminAccess(Long userId) {

        User user = findUser(userId);

        return user.getRoles().stream()
                .map(Role::getName)
                .anyMatch(roleName -> roleName.equals(RoleName.ADMIN));
    }

    private User findUser(Long userId) {
        return userRepository.findUserById(userId).orElseThrow(
                () -> new EntityNotFoundException("User not found by id: " + userId));
    }
}
