package com.ignitis.chat.persistance;

import com.ignitis.chat.persistance.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Modifying
    @Query(
            value = "INSERT INTO message (content, sent_at, user_id, channel_id) " +
                    "VALUES (:content, :sentAt, :userId, :channelId)",
            nativeQuery = true
    )
    void saveMessage(@Param("content") String message,
                     @Param("sentAt") LocalDateTime sentAt,
                     @Param("userId") Long userId,
                     @Param("channelId") Long channelId);

    @Query(
            value = "SELECT * FROM message ORDER BY sent_at DESC",
            nativeQuery = true
    )
    List<Message> findAllMessagesSortedBySentAtDesc();
}
