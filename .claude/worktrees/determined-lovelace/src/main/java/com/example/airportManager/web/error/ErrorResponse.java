package com.example.airportManager.web.error;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        Instant timestamp,
        String path,
        int status,
        String code,
        String message,
        List<String> details
) {}
