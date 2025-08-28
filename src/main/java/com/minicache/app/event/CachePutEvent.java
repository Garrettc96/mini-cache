package com.minicache.app.event;

import java.time.Instant;

public class CachePutEvent extends CacheEvent {
    private final String value;

    public CachePutEvent(String key, String value, Instant timestamp, boolean isReplica) {
        super(key, timestamp, isReplica);
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
