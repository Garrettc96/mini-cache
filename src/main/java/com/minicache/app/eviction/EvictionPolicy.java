package com.minicache.app.eviction;

public interface EvictionPolicy<K> {
    void onGet(K key);
    void onPut(K key);
    void onRemove(K key);
    K evictIfNeeded(); // Returns the key to evict, or null if none
}