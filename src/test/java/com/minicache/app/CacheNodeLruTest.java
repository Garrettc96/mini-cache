package com.minicache.app;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CacheNodeLruTest {
    @Test
    void testLruEviction() {
        int maxSize = 3;
        CacheNode cache = new CacheNode(maxSize);

        // Fill cache to max size
        cache.put("a", "1"); // a
        cache.put("b", "2"); // a, b
        cache.put("c", "3"); // a, b, c

        // Access 'a' to make it most recently used
        cache.get("a");        // b, c, a

        // Add another item, should evict 'b' (the least recently used)
        cache.put("d", "4");  // c, a, d

        assertNull(cache.get("b"), "'b' should have been evicted");
        assertEquals("1", cache.get("a"));
        assertEquals("3", cache.get("c"));
        assertEquals("4", cache.get("d"));
        assertEquals(3, cache.size());
    }
} 