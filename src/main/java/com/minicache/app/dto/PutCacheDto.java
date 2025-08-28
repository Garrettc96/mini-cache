package com.minicache.app.dto;

import java.time.Instant;

public record PutCacheDto(
    String value,
    Instant timestamp
) {
    public PutCacheDto(String value) {
        this(value, Instant.now());
    }
}
