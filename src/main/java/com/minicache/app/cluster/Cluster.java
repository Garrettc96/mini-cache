package com.minicache.app.cluster;

import java.time.OffsetDateTime;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Cluster {
    
    private String name;
    private ClusterStatus status;
    private OffsetDateTime lastHealthyHeartbeat;

    public Cluster(String name) {
        this.name = name;
    }

    public void setStatus(ClusterStatus newStatus) {
        this.status = newStatus;
        if (newStatus.equals(ClusterStatus.HEALTHY)) {
            this.lastHealthyHeartbeat = OffsetDateTime.now();
        }
    }
}
