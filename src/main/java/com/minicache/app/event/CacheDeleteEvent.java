package com.minicache.app.event;

import java.time.Instant;

public class CacheDeleteEvent extends CacheEvent {
    
    public CacheDeleteEvent(String key, Instant timestamp, boolean isReplica) {
        super(key, timestamp, isReplica);
    }
}
