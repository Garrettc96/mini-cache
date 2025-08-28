package com.minicache.app;

import com.minicache.app.eviction.EvictionPolicy;
import com.minicache.app.eviction.LruEvictionPolicy;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;

@Component
public class CacheNode {
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final EvictionPolicy<String> evictionPolicy;
    private final int maxSize;

    public CacheNode() {
        this(100); // default max size
    }

    public CacheNode(int maxSize) {
        this.evictionPolicy = new LruEvictionPolicy<>(maxSize);
        this.maxSize = maxSize;
    }

    public String get(String key) {
        evictionPolicy.onGet(key);
        CacheEntry entry = cache.get(key);
        return entry != null ? entry.getValue() : null;
    }

    public CacheEntry getEntry(String key) {
        evictionPolicy.onGet(key);
        return cache.get(key);
    }

    public boolean put(String key, String value, Instant timestamp) {
        CacheEntry existingEntry = cache.get(key);
        
        // Idempotency check: only update if timestamp is newer or equal
        if (existingEntry != null && timestamp.isBefore(existingEntry.getTimestamp())) {
            return false; // Reject older timestamp
        }
        
        cache.put(key, new CacheEntry(value, timestamp));
        evictionPolicy.onPut(key);
        String evictKey = evictionPolicy.evictIfNeeded();
        if (evictKey != null) {
            cache.remove(evictKey);
        }
        return true;
    }

    public void put(String key, String value) {
        put(key, value, Instant.now());
    }

    public void delete(String key) {
        cache.remove(key);
        evictionPolicy.onRemove(key);
    }

    public int size() {
        return cache.size();
    }
} 