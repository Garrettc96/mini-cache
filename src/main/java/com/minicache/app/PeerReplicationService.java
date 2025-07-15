package com.minicache.app;

import com.minicache.app.ClusterManager;
import com.minicache.app.dto.PutCacheDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PeerReplicationService {
    private static final Logger logger = LoggerFactory.getLogger(PeerReplicationService.class);
    private final ClusterManager clusterManager;
    private final RestTemplate restTemplate;

    @Autowired
    public PeerReplicationService(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
        this.restTemplate = new RestTemplate();
    }

    public void replicatePut(String key, String value) {
        PutCacheDto dto = new PutCacheDto(value);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PutCacheDto> entity = new HttpEntity<>(dto, headers);
        for (String peer : clusterManager.getPeers()) {
            try {
                String url = "http://" + peer + "/cache/" + key + "?replica=true";
                restTemplate.put(url, entity);
            } catch (Exception e) {
                logger.warn("Failed to replicate PUT to peer {}: {}", peer, e.getMessage());
            }
        }
    }

    public void replicateDelete(String key) {
        for (String peer : clusterManager.getPeers()) {
            try {
                String url = "http://" + peer + "/cache/" + key + "?replica=true";
                restTemplate.delete(url);
            } catch (Exception e) {
                logger.warn("Failed to replicate DELETE to peer {}: {}", peer, e.getMessage());
            }
        }
    }
} 