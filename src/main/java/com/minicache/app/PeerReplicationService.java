package com.minicache.app;

import com.minicache.app.cluster.Cluster;
import com.minicache.app.cluster.ClusterManager;
import com.minicache.app.dto.PutCacheDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.Instant;

@Service
@Slf4j
public class PeerReplicationService {

    private final ClusterManager clusterManager;
    private final RestTemplate restTemplate;

    @Autowired
    public PeerReplicationService(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
        this.restTemplate = new RestTemplate();
    }

    public void replicatePut(String key, String value, Instant timestamp) {
        PutCacheDto dto = new PutCacheDto(value, timestamp);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PutCacheDto> entity = new HttpEntity<>(dto, headers);
        for (Cluster peer : clusterManager.getPeers()) {
            try {
                String url = "http://" + peer.getName() + "/cache/" + key + "?replica=true";
                restTemplate.put(url, entity);
            } catch (Exception e) {
                log.warn("Failed to replicate PUT to peer {}: {}", peer, e.getMessage());
            }
        }
    }

    public void replicatePut(String key, String value) {
        replicatePut(key, value, Instant.now());
    }

    public void replicateDelete(String key) {
        for (Cluster peer : clusterManager.getPeers()) {
            try {
                String url = "http://" + peer.getName() + "/cache/" + key + "?replica=true";
                restTemplate.delete(url);
            } catch (Exception e) {
                log.warn("Failed to replicate DELETE to peer {}: {}", peer, e.getMessage());
            }
        }
    }
} 