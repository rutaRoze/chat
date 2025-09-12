package com.ignitis.chat.persistance;

import com.ignitis.chat.persistance.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Modifying
    @Transactional
    @Query(
            value = "INSERT INTO message (message_content, sent_at, user_id, channel_id) " +
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


    @Query(value = """
                SELECT
                    u.user_id AS userId,
                    u.username AS username,
                    COUNT(m.message_id) AS messageCount,
                    MIN(m.sent_at) AS oldestMessageTime,
                    MAX(m.sent_at) AS latestMessageTime,
                    AVG(LENGTH(m.message_content)) AS averageMessageLength,
                    (
                        SELECT m2.message_content
                        FROM message m2
                        WHERE m2.user_id = u.user_id
                        ORDER BY m2.sent_at DESC
                        LIMIT 1
                    ) AS latestMessage
                FROM users u
                LEFT JOIN message m ON u.user_id = m.user_id
                GROUP BY u.user_id, u.username
            """, nativeQuery = true)
    List<Object[]> findUserStatisticsRaw();
}
