package com.eloka.cqrs.sync.controller;

import com.eloka.cqrs.sync.service.SyncStateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SyncMonitoringController {

    private final SyncStateService syncStateService;
    private final String configuredRegion;
    private final int configuredPartition;

    public SyncMonitoringController(
            SyncStateService syncStateService,
            @Value("${cqrs.region}") String configuredRegion,
            @Value("${cqrs.kafka.partition}") int configuredPartition
    ) {
        this.syncStateService = syncStateService;
        this.configuredRegion = configuredRegion;
        this.configuredPartition = configuredPartition;
    }

    @GetMapping("/sync-state")
    public Map<String, Object> getState() {
        return Map.of(
                "configuredRegion", configuredRegion,
                "configuredPartition", configuredPartition,
                "processedMessages", syncStateService.processedMessages(),
                "lastEvent", syncStateService.lastEvent()
        );
    }
}
