package com.minicache.app.listener;

import com.minicache.app.PeerReplicationService;
import com.minicache.app.event.CacheDeleteEvent;
import com.minicache.app.event.CachePutEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReplicationEventListener {

    private final PeerReplicationService peerReplicationService;

    @Autowired
    public ReplicationEventListener(PeerReplicationService peerReplicationService) {
        this.peerReplicationService = peerReplicationService;
    }

    @EventListener
    public void handleCachePutEvent(CachePutEvent event) {
        log.debug("Replicating PUT event for key: {}", event.getKey());
        peerReplicationService.replicatePut(event.getKey(), event.getValue(), event.getTimestamp());
    }

    @EventListener
    public void handleCacheDeleteEvent(CacheDeleteEvent event) {
        log.debug("Replicating DELETE event for key: {}", event.getKey());
        peerReplicationService.replicateDelete(event.getKey());
    }
}
