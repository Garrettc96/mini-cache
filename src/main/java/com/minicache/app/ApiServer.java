package com.minicache.app;

import com.minicache.app.dto.GetCacheDto;
import com.minicache.app.dto.PutCacheDto;
import com.minicache.app.event.CacheDeleteEvent;
import com.minicache.app.event.CachePutEvent;
import com.minicache.app.exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import java.time.Instant;

@RestController
@RequestMapping("/cache")
@Slf4j
public class ApiServer {
    private final CacheNode cacheNode;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public ApiServer(CacheNode cacheNode, ApplicationEventPublisher eventPublisher) {
        this.cacheNode = cacheNode;
        this.eventPublisher = eventPublisher;
    }

    @GetMapping("/{key}")
    public GetCacheDto get(@PathVariable String key) {
        CacheEntry entry = cacheNode.getEntry(key);
        if (entry == null) {
            log.warn("Key {} not found", key);
            throw new ResourceNotFoundException("Key " + key + " not found");
        }
        log.info("Key {} found", key);
        return new GetCacheDto(entry.getValue(), entry.getTimestamp());
    }

    @PutMapping("/{key}")
    public ResponseEntity<Void> put(@PathVariable String key, @RequestBody PutCacheDto dto, HttpServletRequest request) {
        boolean success = cacheNode.put(key, dto.value(), dto.timestamp());
        
        if (!success) {
            log.warn("Rejected PUT for key {} with older timestamp", key);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        // Only publish events for non-replica operations to reduce overhead
        boolean isReplica = isReplica(request);
        if (!isReplica) {
            CachePutEvent event = new CachePutEvent(key, dto.value(), dto.timestamp(), false);
            eventPublisher.publishEvent(event);
            log.debug("Published PUT event for key: {}", key);
        } else {
            log.debug("Skipping event publication for replica PUT operation on key: {}", key);
        }
        
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{key}")
    public void delete(@PathVariable String key, HttpServletRequest request) {
        cacheNode.delete(key);
        
        // Only publish events for non-replica operations to reduce overhead
        boolean isReplica = isReplica(request);
        if (!isReplica) {
            CacheDeleteEvent event = new CacheDeleteEvent(key, Instant.now(), false);
            eventPublisher.publishEvent(event);
            log.debug("Published DELETE event for key: {}", key);
        } else {
            log.debug("Skipping event publication for replica DELETE operation on key: {}", key);
        }
    }

    private boolean isReplica(HttpServletRequest request) {
        String replica = request.getParameter("replica");
        return "true".equalsIgnoreCase(replica);
    }
} 