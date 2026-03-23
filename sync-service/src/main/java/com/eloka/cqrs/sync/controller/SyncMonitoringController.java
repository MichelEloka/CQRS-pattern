package com.eloka.cqrs.sync.controller;

import com.eloka.cqrs.sync.service.SyncStateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SyncMonitoringController {

    private final SyncStateService syncStateService;

    public SyncMonitoringController(SyncStateService syncStateService) {
        this.syncStateService = syncStateService;
    }

    @GetMapping("/sync-state")
    public Map<String, Object> getState() {
        return Map.of(
                "processedMessages", syncStateService.processedMessages(),
                "lastEvent", syncStateService.lastEvent()
        );
    }
}

