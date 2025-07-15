package com.minicache.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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
        restTemplate.put(putUrl, new PutDto(value));

        // Wait for replication
        Thread.sleep(2000);

        // Get value from cache2 and cache3
        GetDto result2 = restTemplate.getForObject(getUrl1, GetDto.class);
        GetDto result3 = restTemplate.getForObject(getUrl2, GetDto.class);

        assertNotNull(result2);
        assertEquals(value, result2.value);
        assertNotNull(result3);
        assertEquals(value, result3.value);
    }

    static class PutDto {
        public String value;
        public PutDto(String value) { this.value = value; }
        public PutDto() {}
    }
    static class GetDto {
        public String value;
        public GetDto() {}
    }
} 