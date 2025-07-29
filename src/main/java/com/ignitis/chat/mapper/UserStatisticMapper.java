package com.ignitis.chat.mapper;

import com.ignitis.chat.dto.UserStatisticResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component
public class UserStatisticMapper {

    public UserStatisticResponse mapToUseStatisticResponse(Object[] userData) {

        UserStatisticResponse response = UserStatisticResponse.builder()
                .userId((Long) userData[0])
                .username((String) userData[1])
                .messageCount(userData[2] != null ? ((Number) userData[2]).intValue() : 0)
                .oldestMessageTime(toLocalDateTime(userData[3]))
                .latestMessageTime(toLocalDateTime(userData[4]))
                .averageMessageLength(userData[5] != null ? ((BigDecimal) userData[5]).setScale(0, RoundingMode.HALF_UP).intValue() : 0)
                .latestMessage((String) userData[6])
                .build();

        return response;
    }

    private LocalDateTime toLocalDateTime(Object object) {
        return object instanceof Timestamp ? ((Timestamp) object).toLocalDateTime() : null;
    }
}
