package com.ignitis.chat.controller;

import com.ignitis.chat.dto.UserStatisticResponse;
import com.ignitis.chat.persistance.model.User;
import com.ignitis.chat.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin related endpoints", description = "Create, delete users, get user statistics")
@RestController
@RequestMapping("/admin")
@Validated
@RequiredArgsConstructor
public class AdminController {

    private static final String FORBIDDEN_MESSAGE = "Access denied. Administrator privileges are required.";

    private final AdminService adminService;

    @Operation(summary = "Create new user using unique username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(responseCode = "400", description = "Invalid username"),
            @ApiResponse(responseCode = "403", description = "Access denied: Administrator privileges required"),
            @ApiResponse(responseCode = "404", description = "User or user role not found"),
            @ApiResponse(responseCode = "409", description = "User already exists")})
    @PostMapping("/user")
    public ResponseEntity<String> createUser(
            @Parameter(description = "Admin user id. Requires administrative privileges.")
            @Min(1)
            @RequestParam Long adminId,

            @Parameter(description = "Unique username for the new user. " +
                    "Max length 10 characters. Cannot be blank.")
            @NotNull
            @NotBlank
            @Size(max = 10)
            @RequestParam String username) {

        if (!adminService.hasAdminAccess(adminId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(FORBIDDEN_MESSAGE);
        }

        User user = adminService.createUser(username);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(String.format("User %s created", user.getUsername()));
    }

    @Operation(summary = "Soft-delete a user by anonymizing their username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User has been successfully anonymized"),
            @ApiResponse(responseCode = "403", description = "Access denied: Administrator privileges required"),
            @ApiResponse(responseCode = "404", description = "User not found")})
    @DeleteMapping("user/{userId}")
    public ResponseEntity<String> deleteUser(
            @Parameter(description = "Admin user id. Requires administrative privileges.")
            @Min(1)
            @RequestParam Long adminId,

            @Parameter(description = "Unique identifier of user.")
            @Min(1)
            @PathVariable Long userId) {

        if (!adminService.hasAdminAccess(adminId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(FORBIDDEN_MESSAGE);
        }

        adminService.deleteUser(userId);

        return ResponseEntity.ok("User deleted successfully");
    }

    @Operation(
            summary = "Retrieve message-related statistics for all users",
            description = """
                    Returns aggregated data such as message count,
                    first and latest message timestamps, average message length,
                    and the content of each user's most recent message.
                    Only accessible to Admin user.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns a list of statistics ordered by user",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UserStatisticResponse.class)))),
            @ApiResponse(responseCode = "403", description = "Access denied: Administrator privileges required")})
    @GetMapping("/user/statistic")
    public ResponseEntity<Object> getStatisticByUser(
            @Parameter(description = "Admin user id. Requires administrative privileges.")
            @Min(1)
            @RequestParam Long adminId) {

        if (!adminService.hasAdminAccess(adminId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(FORBIDDEN_MESSAGE);
        }

        List<UserStatisticResponse> response = adminService.getUserStatistic();

        return ResponseEntity.ok(response);
    }
}
