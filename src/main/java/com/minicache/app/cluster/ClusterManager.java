package com.minicache.app.cluster;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ClusterManager {

    @Value("${cache.peers:}")
    private String peersConfig;

    @Value("${cluster.heartbeat.timeout}")
    private int heartbeatTimeout;

    @Value("${cluster.heartbeat.interval}")
    private long heartbeatInterval;

    private static long initialHeartbeatDelay = 10000;

    private List<Cluster> peers;
    AsyncHttpClient client = Dsl.asyncHttpClient(
            Dsl.config()
            .setConnectTimeout(Duration.of(heartbeatTimeout, ChronoUnit.MILLIS))
            .build()
    );

    private ScheduledThreadPoolExecutor heartbeatThreadPool;

    @PostConstruct
    private void init() {
        if (peersConfig == null || peersConfig.isBlank()) {
            peers = List.of();
        } else {
            peers = Arrays.stream(peersConfig.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(s -> new Cluster(s))
                    .collect(Collectors.toList());
        }
        heartbeatThreadPool = new ScheduledThreadPoolExecutor(1);
        peers.stream().forEach(
            peer -> heartbeatThreadPool.scheduleAtFixedRate(
                () -> doHeartbeat(peer),
                initialHeartbeatDelay,
                heartbeatInterval,
                TimeUnit.MILLISECONDS
        ));
    }

    public List<Cluster> getPeers() {
        return peers;
    }

    private void doHeartbeat(Cluster cluster) {
        this.client.executeRequest(
           Dsl.get("http://" + cluster.getName() + "/actuator/health")
        )
        .toCompletableFuture()
        .thenCompose(response -> {
            if (HttpStatus.valueOf(response.getStatusCode()).is2xxSuccessful()) {
                log.info("Received health heartbeat for cluster {}", cluster);
                cluster.setStatus(ClusterStatus.HEALTHY);
            } else {
                log.warn("Did not receive heartbeat for cluste: {}", cluster);
            }
            
            return CompletableFuture.completedFuture(response);
        });
    }
}