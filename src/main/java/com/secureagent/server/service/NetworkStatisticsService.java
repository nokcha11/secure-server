package com.secureagent.server.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secureagent.server.dto.HourlyConnectionStatResponse;
import com.secureagent.server.entity.AgentNetworkConnection;
import com.secureagent.server.repository.AgentNetworkConnectionRepository;

@Service
public class NetworkStatisticsService {

    private static final int HOURS_TO_DISPLAY = 24;

    private static final DateTimeFormatter LABEL_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "MM-dd HH:00"
            );

    private final AgentNetworkConnectionRepository repository;

    public NetworkStatisticsService(
            AgentNetworkConnectionRepository repository) {

        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<HourlyConnectionStatResponse>
            getHourlyConnectionStats(
                    String computerName) {

        /*
         * 현재 시간을 시간 단위로 맞춥니다.
         *
         * 예:
         * 10:37:25 → 10:00:00
         */
        LocalDateTime currentHour =
                LocalDateTime.now()
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);

        /*
         * 현재 시간을 포함한 최근 24시간의
         * 첫 번째 시간을 계산합니다.
         */
        LocalDateTime firstHour =
                currentHour.minusHours(
                        HOURS_TO_DISPLAY - 1
                );

        List<AgentNetworkConnection> connectionList =
                repository
                        .findByComputerNameOrderByLastSeenAtDesc(
                                computerName
                        );

        List<HourlyConnectionStatResponse> result =
                new ArrayList<>();

        for (int index = 0;
                index < HOURS_TO_DISPLAY;
                index++) {

            LocalDateTime hourStart =
                    firstHour.plusHours(index);

            LocalDateTime hourEnd =
                    hourStart.plusHours(1);

            int connectionCount =
                    (int) connectionList.stream()
                            .filter(connection ->
                                    isFirstSeenDuringHour(
                                            connection,
                                            hourStart,
                                            hourEnd
                                    )
                            )
                            .count();

            result.add(
                    new HourlyConnectionStatResponse(
                            hourStart.format(
                                    LABEL_FORMATTER
                            ),
                            connectionCount
                    )
            );
        }

        return result;
    }

    private boolean isFirstSeenDuringHour(
            AgentNetworkConnection connection,
            LocalDateTime hourStart,
            LocalDateTime hourEnd) {

        LocalDateTime firstSeenAt =
                connection.getFirstSeenAt();

        if (firstSeenAt == null) {
            return false;
        }

        return !firstSeenAt.isBefore(hourStart)
                && firstSeenAt.isBefore(hourEnd);
    }
}