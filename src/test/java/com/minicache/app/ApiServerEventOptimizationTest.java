package com.minicache.app;

import com.minicache.app.dto.PutCacheDto;
import com.minicache.app.event.CacheDeleteEvent;
import com.minicache.app.event.CachePutEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApiServerEventOptimizationTest {

    @Mock
    private CacheNode cacheNode;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ApiServer apiServer;

    @BeforeEach
    void setUp() {
        apiServer = new ApiServer(cacheNode, eventPublisher);
    }

    @Test
    void testNonReplicaPutPublishesEvent() {
        // Given
        String key = "test-key";
        PutCacheDto dto = new PutCacheDto("test-value", Instant.now());
        MockHttpServletRequest request = new MockHttpServletRequest();
        
        when(cacheNode.put(eq(key), eq(dto.value()), eq(dto.timestamp()))).thenReturn(true);

        // When
        apiServer.put(key, dto, request);

        // Then
        verify(eventPublisher).publishEvent(any(CachePutEvent.class));
    }

    @Test
    void testReplicaPutDoesNotPublishEvent() {
        // Given
        String key = "test-key";
        PutCacheDto dto = new PutCacheDto("test-value", Instant.now());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("replica", "true");
        
        when(cacheNode.put(eq(key), eq(dto.value()), eq(dto.timestamp()))).thenReturn(true);

        // When
        apiServer.put(key, dto, request);

        // Then
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void testNonReplicaDeletePublishesEvent() {
        // Given
        String key = "test-key";
        MockHttpServletRequest request = new MockHttpServletRequest();

        // When
        apiServer.delete(key, request);

        // Then
        verify(eventPublisher).publishEvent(any(CacheDeleteEvent.class));
    }

    @Test
    void testReplicaDeleteDoesNotPublishEvent() {
        // Given
        String key = "test-key";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("replica", "true");

        // When
        apiServer.delete(key, request);

        // Then
        verify(eventPublisher, never()).publishEvent(any());
    }
}
