package com.minicache.app.dto;

import java.time.Instant;

public record GetCacheDto(
    String value,
    Instant timestamp
) {
    
}
