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
}
