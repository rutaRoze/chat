package com.ignitis.chat.persistance;

import com.ignitis.chat.persistance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query(
            value = "SELECT * FROM users WHERE user_id = :userId",
            nativeQuery = true
    )
    Optional<User> findUserById(@Param("userId") Long userId);

    @Query(
            value = "SELECT EXISTS (SELECT 1 FROM users WHERE username ILIKE :username)",
            nativeQuery = true
    )
    boolean doesUserExistIgnoreCase(@Param("username") String username);

    @Modifying
    @Query(
            value = "INSERT INTO users (username) VALUES (:username)",
            nativeQuery = true)
    void saveUser(@Param("username") String username);

    @Query(
            value = "SELECT user_id FROM users WHERE username = :username",
            nativeQuery = true)
    Long findUserIdByUsername(@Param("username") String username);

    @Modifying
    @Query(
            value = "INSERT INTO user_roles (user_id, role_id) VALUES (:userId, :roleId)",
            nativeQuery = true)
    void linkUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    @Modifying
    @Query(value = """
            UPDATE users
            SET username = :newUsername, deleted_at = :deletedAt
            WHERE user_id = :userId
            """, nativeQuery = true)
    void softDeleteUser(@Param("userId") Long userId,
                        @Param("newUsername") String newUsername,
                        @Param("deletedAt") LocalDateTime deletedAt);
}
