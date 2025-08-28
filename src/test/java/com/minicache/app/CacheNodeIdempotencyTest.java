package com.minicache.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class CacheNodeIdempotencyTest {

    private CacheNode cacheNode;

    @BeforeEach
    void setUp() {
        cacheNode = new CacheNode(100);
    }

    @Test
    void testIdempotencyWithTimestamps() {
        String key = "test-key";
        Instant newerTimestamp = Instant.parse("2024-12-01T12:00:00Z");
        Instant olderTimestamp = Instant.parse("2024-01-01T12:00:00Z");
        
        // First PUT with newer timestamp
        boolean newerResult = cacheNode.put(key, "newer_value", newerTimestamp);
        assertTrue(newerResult, "Newer timestamp should be accepted");
        
        // Second PUT with older timestamp - should be rejected
        boolean olderResult = cacheNode.put(key, "older_value", olderTimestamp);
        assertFalse(olderResult, "Older timestamp should be rejected");
        
        // Verify the newer value is still stored
        CacheEntry entry = cacheNode.getEntry(key);
        assertNotNull(entry);
        assertEquals("newer_value", entry.getValue());
        assertEquals(newerTimestamp, entry.getTimestamp());
    }

    @Test
    void testSameTimestampUpdate() {
        String key = "same-timestamp-key";
        Instant timestamp = Instant.parse("2024-06-01T12:00:00Z");
        
        // First PUT
        boolean firstResult = cacheNode.put(key, "first_value", timestamp);
        assertTrue(firstResult, "First PUT should be accepted");
        
        // Second PUT with same timestamp - should be accepted (idempotent)
        boolean secondResult = cacheNode.put(key, "second_value", timestamp);
        assertTrue(secondResult, "Same timestamp should be accepted");
        
        // Verify the second value is stored
        CacheEntry entry = cacheNode.getEntry(key);
        assertNotNull(entry);
        assertEquals("second_value", entry.getValue());
        assertEquals(timestamp, entry.getTimestamp());
    }

    @Test
    void testNewerTimestampOverwrites() {
        String key = "newer-overwrites-key";
        Instant olderTimestamp = Instant.parse("2024-01-01T12:00:00Z");
        Instant newerTimestamp = Instant.parse("2024-12-01T12:00:00Z");
        
        // First PUT with older timestamp
        boolean olderResult = cacheNode.put(key, "older_value", olderTimestamp);
        assertTrue(olderResult, "Older timestamp should be accepted initially");
        
        // Second PUT with newer timestamp - should overwrite
        boolean newerResult = cacheNode.put(key, "newer_value", newerTimestamp);
        assertTrue(newerResult, "Newer timestamp should overwrite");
        
        // Verify the newer value is stored
        CacheEntry entry = cacheNode.getEntry(key);
        assertNotNull(entry);
        assertEquals("newer_value", entry.getValue());
        assertEquals(newerTimestamp, entry.getTimestamp());
    }
}
