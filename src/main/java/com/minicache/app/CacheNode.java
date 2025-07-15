package com.minicache.app;

import com.minicache.app.eviction.EvictionPolicy;
import com.minicache.app.eviction.LruEvictionPolicy;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CacheNode {
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();
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
        return cache.get(key);
    }

    public void put(String key, String value) {
        cache.put(key, value);
        evictionPolicy.onPut(key);
        String evictKey = evictionPolicy.evictIfNeeded();
        if (evictKey != null) {
            cache.remove(evictKey);
        }
    }

    public void delete(String key) {
        cache.remove(key);
        evictionPolicy.onRemove(key);
    }

    public int size() {
        return cache.size();
    }
} 