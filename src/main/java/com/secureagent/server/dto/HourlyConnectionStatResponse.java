package com.secureagent.server.dto;

public record HourlyConnectionStatResponse(

        String timeLabel,

        int connectionCount

) {
}