package com.minicache.app.eviction;

import java.util.LinkedHashMap;
import java.util.Map;

public class LruEvictionPolicy<K> implements EvictionPolicy<K> {
    private final int maxSize;
    private final LinkedHashMap<K, Boolean> accessOrder;

    public LruEvictionPolicy(int maxSize) {
        this.maxSize = maxSize;
        this.accessOrder = new LinkedHashMap<>(16, 0.75f, true);
    }

    @Override
    public synchronized void onGet(K key) {
        if (accessOrder.containsKey(key)) {
            accessOrder.get(key); // reorder
        }
    }

    @Override
    public synchronized void onPut(K key) {
        accessOrder.put(key, Boolean.TRUE);
    }

    @Override
    public synchronized void onRemove(K key) {
        accessOrder.remove(key);
    }

    @Override
    public synchronized K evictIfNeeded() {
        if (accessOrder.size() > maxSize) {
            K eldest = accessOrder.keySet().iterator().next();
            accessOrder.remove(eldest);
            return eldest;
        }
        return null;
    }
} 