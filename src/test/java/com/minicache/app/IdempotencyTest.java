package com.minicache.app;

import com.minicache.app.dto.GetCacheDto;
import com.minicache.app.dto.PutCacheDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "cache.peers=",
    "cluster.heartbeat.interval=10000",
    "cluster.heartbeat.timeout=3000"
})
public class IdempotencyTest {

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    public void testIdempotencyWithTimestamps() {
        String baseUrl = "http://localhost:" + port + "/cache/test-key";
        
        // First PUT with newer timestamp
        Instant newerTimestamp = Instant.parse("2024-12-01T12:00:00Z");
        String newerValue = "newer_value";
        
        PutCacheDto newerDto = new PutCacheDto(newerValue, newerTimestamp);
        restTemplate.put(baseUrl, newerDto);
        
        // Second PUT with older timestamp - should be rejected
        Instant olderTimestamp = Instant.parse("2024-01-01T12:00:00Z");
        String olderValue = "older_value";
        
        PutCacheDto olderDto = new PutCacheDto(olderValue, olderTimestamp);
        ResponseEntity<Void> olderResponse = restTemplate.postForEntity(baseUrl, olderDto, Void.class);
        
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, olderResponse.getStatusCode());
        
        // Verify the newer value is still stored
        GetCacheDto getResponse = restTemplate.getForObject(baseUrl, GetCacheDto.class);
        assertNotNull(getResponse);
        assertEquals(newerValue, getResponse.value());
        assertEquals(newerTimestamp, getResponse.timestamp());
    }

    @Test
    public void testSameTimestampUpdate() {
        String baseUrl = "http://localhost:" + port + "/cache/same-timestamp-key";
        Instant timestamp = Instant.parse("2024-06-01T12:00:00Z");
        
        // First PUT
        PutCacheDto firstDto = new PutCacheDto("first_value", timestamp);
        restTemplate.put(baseUrl, firstDto);
        
        // Second PUT with same timestamp - should be accepted (idempotent)
        PutCacheDto secondDto = new PutCacheDto("second_value", timestamp);
        restTemplate.put(baseUrl, secondDto);
        
        // Verify the second value is stored
        GetCacheDto getResponse = restTemplate.getForObject(baseUrl, GetCacheDto.class);
        assertNotNull(getResponse);
        assertEquals("second_value", getResponse.value());
    }
}
