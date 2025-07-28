package com.ignitis.chat.persistance;

import com.ignitis.chat.persistance.model.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {

    @Query(
            value = "SELECT * FROM channel WHERE channel_id = :channelId",
            nativeQuery = true
    )
    Optional<Channel> findChannelById(@Param("channelId") Long channelId);
}
