package com.minicache.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.minicache.app.dto.GetCacheDto;
import com.minicache.app.dto.PutCacheDto;

import java.io.File;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class ReplicationIntegrationTest {
    @Container
    public static DockerComposeContainer<?> compose =
            new DockerComposeContainer<>(new File("docker-compose.yml"))
                    .withExposedService("cache1", 8080)
                    .withExposedService("cache2", 8081)
                    .withExposedService("cache3", 8082);

    private static final RestTemplate restTemplate = new RestTemplate();

    @BeforeAll
    public static void setUp() {
        compose.start();
    }

    @AfterAll
    public static void tearDown() {
        compose.stop();
    }

    @Test
    void testReplicationAcrossNodes() throws InterruptedException {
        String key = "foo";
        String value = "bar";
        String putUrl = "http://localhost:8080/cache/" + key;
        String getUrl1 = "http://localhost:8081/cache/" + key;
        String getUrl2 = "http://localhost:8082/cache/" + key;
        // Put value on cache1
        HttpHeaders headers = new HttpHeaders();

        HttpEntity<PutCacheDto> requestUpdate = new HttpEntity<>(new PutCacheDto(value), headers);

        restTemplate.put(putUrl, requestUpdate);

        // Wait for replication
        Thread.sleep(2000);

        // Get value from cache2 and cache3
        GetCacheDto result2 = restTemplate.getForObject(getUrl1, GetCacheDto.class);
        GetCacheDto result3 = restTemplate.getForObject(getUrl2, GetCacheDto.class);

        assertNotNull(result2);
        assertEquals(value, result2.value());
        assertNotNull(result3);
        assertEquals(value, result3.value());
    }
} 