package com.minicache.app;

import java.time.Instant;

public class CacheEntry {
    private final String value;
    private final Instant timestamp;

    public CacheEntry(String value, Instant timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }

    public String getValue() {
        return value;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
