package com.ignitis.chat.service;

import com.ignitis.chat.dto.UserStatisticResponse;
import com.ignitis.chat.exception.UserAlreadyExistsException;
import com.ignitis.chat.mapper.UserStatisticMapper;
import com.ignitis.chat.persistance.MessageRepository;
import com.ignitis.chat.persistance.RoleRepository;
import com.ignitis.chat.persistance.UserRepository;
import com.ignitis.chat.persistance.model.Role;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private UserStatisticMapper userStatisticMapper;

    @InjectMocks
    private AdminService adminService;

    @Test
    void createUser_shouldCreateUserWhenValidUsernameAndRoleExists() {
        Role mockRole = buildRole();

        when(userRepository.doesUserExistIgnoreCase(USERNAME)).thenReturn(false);
        when(roleRepository.findRoleByName(USER_ROLE)).thenReturn(Optional.of(mockRole));
        when(userRepository.findUserIdByUsername(USERNAME)).thenReturn(USER_ID);

        User result = adminService.createUser(USERNAME);

        assertEquals(USERNAME, result.getUsername());
        assertTrue(result.getRoles().contains(mockRole));

        verify(userRepository).saveUser(USERNAME);
        verify(userRepository).linkUserRole(USER_ID, mockRole.getId());
    }

    @Test
    void createUser_shouldThrowExceptionWhenUserAlreadyExists() {
        when(userRepository.doesUserExistIgnoreCase(USERNAME)).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () ->
                adminService.createUser(USERNAME));

        assertTrue(exception.getMessage().contains("already exists"));

        verify(userRepository, never()).saveUser(any());
    }

    @Test
    void createUser_shouldThrowExceptionWhenUserRoleNotFound() {
        when(userRepository.doesUserExistIgnoreCase(USERNAME)).thenReturn(false);
        when(roleRepository.findRoleByName(USER_ROLE)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                adminService.createUser(USERNAME));

        assertTrue(exception.getMessage().contains("role not found"));
    }

    @Test
    void deleteUser_shouldSoftDeleteUser() {
        User mockUser = buildUser(USER_ID, USERNAME);

        when(userRepository.findUserById(mockUser.getId())).thenReturn(Optional.of(mockUser));
        doNothing().when(userRepository).softDeleteUser(eq(mockUser.getId()), anyString(), any());

        adminService.deleteUser(mockUser.getId());

        verify(userRepository).softDeleteUser(eq(mockUser.getId()), eq("anonymous"), any(LocalDateTime.class));
    }

    @Test
    void getUserStatistic_shouldReturnMappedStatistics() {
        Object[] mockRawData1 = new Object[]{USER_ID, USERNAME, 10, LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now().minusMinutes(160), 20, MESSAGE};
        Object[] mockRawData2 = new Object[]{2L, USERNAME2, 5, LocalDateTime.now().minusMinutes(3),
                LocalDateTime.now().minusMinutes(100), 16, MESSAGE};

        List<Object[]> rawList = List.of(mockRawData1, mockRawData2);

        UserStatisticResponse mockResponse1 = buildUserStatisticResponse(USER_ID, USERNAME, 10, MESSAGE);
        UserStatisticResponse mockResponse2 = buildUserStatisticResponse(2L, USERNAME2, 5, MESSAGE2);

        when(messageRepository.findUserStatisticsRaw()).thenReturn(rawList);
        when(userStatisticMapper.mapToUseStatisticResponse(rawList.get(0))).thenReturn(mockResponse1);
        when(userStatisticMapper.mapToUseStatisticResponse(rawList.get(1))).thenReturn(mockResponse2);

        List<UserStatisticResponse> results = adminService.getUsersStatistic();

        assertEquals(2, results.size());
        assertEquals(USERNAME, results.get(0).getUsername());
        assertEquals(USERNAME2, results.get(1).getUsername());
    }
}