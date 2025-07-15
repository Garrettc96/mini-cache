package com.minicache.app;

import com.minicache.app.dto.GetCacheDto;
import com.minicache.app.dto.PutCacheDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/cache")
public class ApiServer {
    private final CacheNode cacheNode;
    private final PeerReplicationService peerReplicationService;

    @Autowired
    public ApiServer(CacheNode cacheNode, PeerReplicationService peerReplicationService) {
        this.cacheNode = cacheNode;
        this.peerReplicationService = peerReplicationService;
    }

    @GetMapping("/{key}")
    public GetCacheDto get(@PathVariable String key) {
        String value = cacheNode.get(key);
        if (value == null) {
            throw new ResourceNotFoundException();
        }
        return new GetCacheDto(value);
    }

    @PutMapping("/{key}")
    public void put(@PathVariable String key, @RequestBody PutCacheDto dto, HttpServletRequest request) {
        cacheNode.put(key, dto.value());
        if (!isReplica(request)) {
            peerReplicationService.replicatePut(key, dto.value());
        }
    }

    @DeleteMapping("/{key}")
    public void delete(@PathVariable String key, HttpServletRequest request) {
        cacheNode.delete(key);
        if (!isReplica(request)) {
            peerReplicationService.replicateDelete(key);
        }
    }

    private boolean isReplica(HttpServletRequest request) {
        String replica = request.getParameter("replica");
        return "true".equalsIgnoreCase(replica);
    }

    @ResponseStatus(code = org.springframework.http.HttpStatus.NOT_FOUND)
    static class ResourceNotFoundException extends RuntimeException {}
} 