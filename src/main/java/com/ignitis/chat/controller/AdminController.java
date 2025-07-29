package com.ignitis.chat.controller;

import com.ignitis.chat.dto.UserStatisticResponse;
import com.ignitis.chat.persistance.model.User;
import com.ignitis.chat.service.AdminService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Validated
@RequiredArgsConstructor
public class AdminController {

    private static final String FORBIDDEN_MESSAGE = "Access denied. Administrator privileges are required.";

    private final AdminService adminService;

    @PostMapping("/user")
    public ResponseEntity<String> createUser(
            @Min(1) @RequestParam Long adminId,
            @NotBlank @Size(max = 10) @RequestParam String username) {

        if (!adminService.hasAdminAccess(adminId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(FORBIDDEN_MESSAGE);
        }

        User user = adminService.createUser(username);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(String.format("User %s created", user.getUsername()));
    }

    @DeleteMapping("user/{userId}")
    public ResponseEntity<String> deleteUser(
            @Min(1) @RequestParam Long adminId,
            @Min(1) @PathVariable Long userId) {

        if (!adminService.hasAdminAccess(adminId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(FORBIDDEN_MESSAGE);
        }

        adminService.deleteUser(userId);

        return ResponseEntity.ok("User deleted successfully");
    }

    @GetMapping("/user/statistic")
    public ResponseEntity<Object> getStatisticByUser(
            @Min(1) @RequestParam Long adminId) {

        if (!adminService.hasAdminAccess(adminId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(FORBIDDEN_MESSAGE);
        }

        List<UserStatisticResponse> response = adminService.getUserStatistic();

        return ResponseEntity.ok(response);
    }
}
