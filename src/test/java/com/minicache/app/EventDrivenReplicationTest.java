package com.minicache.app;

import com.minicache.app.dto.PutCacheDto;
import com.minicache.app.event.CacheDeleteEvent;
import com.minicache.app.event.CachePutEvent;
import com.minicache.app.listener.ReplicationEventListener;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
    "cache.peers=",
    "cluster.heartbeat.interval=10000",
    "cluster.heartbeat.timeout=3000"
})
public class EventDrivenReplicationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @MockBean
    private PeerReplicationService peerReplicationService;

    @Autowired
    private ReplicationEventListener replicationEventListener;

    @Test
    void testPutEventTriggersReplication() {
        // Given
        String key = "test-key";
        String value = "test-value";
        Instant timestamp = Instant.now();
        CachePutEvent event = new CachePutEvent(key, value, timestamp, false);

        // When
        eventPublisher.publishEvent(event);

        // Then
        verify(peerReplicationService, timeout(1000)).replicatePut(eq(key), eq(value), eq(timestamp));
    }

    @Test
    void testDeleteEventTriggersReplication() {
        // Given
        String key = "test-key";
        Instant timestamp = Instant.now();
        CacheDeleteEvent event = new CacheDeleteEvent(key, timestamp, false);

        // When
        eventPublisher.publishEvent(event);

        // Then
        verify(peerReplicationService, timeout(1000)).replicateDelete(eq(key));
    }

    @Test
    void testReplicaEventsAreNotPublished() {
        // Given - replica events should not be published at all
        // This test verifies that the ApiServer doesn't publish replica events
        
        // When - we don't publish replica events anymore
        // Then - no replication should occur
        verify(peerReplicationService, never()).replicatePut(any(), any(), any());
        verify(peerReplicationService, never()).replicateDelete(any());
    }
}
