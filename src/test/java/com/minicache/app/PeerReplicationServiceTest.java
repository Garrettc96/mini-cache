package com.minicache.app;

import com.minicache.app.cluster.Cluster;
import com.minicache.app.cluster.ClusterManager;
import com.minicache.app.dto.PutCacheDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PeerReplicationServiceTest {
    @Mock
    private ClusterManager clusterManager;
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private PeerReplicationService peerReplicationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Use reflection to inject the mock RestTemplate
        try {
            var field = PeerReplicationService.class.getDeclaredField("restTemplate");
            field.setAccessible(true);
            field.set(peerReplicationService, restTemplate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testReplicatePutSendsToAllPeers() {
        when(clusterManager.getPeers()).thenReturn(List.of(new Cluster("localhost:8081"), new Cluster("localhost:8082")));
        peerReplicationService.replicatePut("foo", "bar");
        verify(restTemplate, times(1)).put(contains("localhost:8081"), any(HttpEntity.class));
        verify(restTemplate, times(1)).put(contains("localhost:8082"), any(HttpEntity.class));
    }

    @Test
    void testReplicateDeleteSendsToAllPeers() {
        when(clusterManager.getPeers()).thenReturn(List.of(new Cluster("localhost:8081"), new Cluster("localhost:8082")));
        peerReplicationService.replicateDelete("foo");
        verify(restTemplate, times(1)).delete(contains("localhost:8081"));
        verify(restTemplate, times(1)).delete(contains("localhost:8082"));
    }
} 