package com.secureagent.server.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.secureagent.server.dto.HourlyConnectionStatResponse;
import com.secureagent.server.service.NetworkStatisticsService;

@RestController
@RequestMapping("/api/agents")
public class NetworkStatisticsController {

    private final NetworkStatisticsService
            networkStatisticsService;

    public NetworkStatisticsController(
            NetworkStatisticsService
                    networkStatisticsService) {

        this.networkStatisticsService =
                networkStatisticsService;
    }

    @GetMapping(
        "/{computerName}/network-connections/hourly-stats"
    )
    public List<HourlyConnectionStatResponse>
            getHourlyConnectionStats(
                    @PathVariable
                    String computerName) {

        return networkStatisticsService
                .getHourlyConnectionStats(
                        computerName
                );
    }
}