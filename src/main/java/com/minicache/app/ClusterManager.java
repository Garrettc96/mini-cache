package com.minicache.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ClusterManager {
    @Value("${cache.peers:}")
    private String peersConfig;

    private List<String> peers;

    @PostConstruct
    private void init() {
        if (peersConfig == null || peersConfig.isBlank()) {
            peers = List.of();
        } else {
            peers = Arrays.stream(peersConfig.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
    }

    public List<String> getPeers() {
        return peers;
    }
} 