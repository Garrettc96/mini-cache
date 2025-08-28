package com.minicache.app.event;

import java.time.Instant;

public abstract class CacheEvent {
    private final String key;
    private final Instant timestamp;
    private final boolean isReplica;

    protected CacheEvent(String key, Instant timestamp, boolean isReplica) {
        this.key = key;
        this.timestamp = timestamp;
        this.isReplica = isReplica;
    }

    public String getKey() {
        return key;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public boolean isReplica() {
        return isReplica;
    }
}
