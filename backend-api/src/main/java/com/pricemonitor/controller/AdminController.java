package com.pricemonitor.controller;

import com.pricemonitor.service.PriceSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private PriceSyncService priceSyncService;

    // Call this to manually trigger price sync during testing:
    // POST http://localhost:8080/admin/sync
    @PostMapping("/sync")
    public ResponseEntity<Map<String, String>> triggerSync() {
        new Thread(() -> priceSyncService.triggerManualSync()).start();
        return ResponseEntity.ok(Map.of(
            "status", "sync started",
            "message", "Check server logs for progress"
        ));
    }
}
