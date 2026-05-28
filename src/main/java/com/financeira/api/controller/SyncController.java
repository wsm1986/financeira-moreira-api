package com.financeira.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SyncController {

    private static final String BUILD = "fix-upsert-v3-debug";

    @GetMapping("/sync")
    public String sync() {
        return "Sincronizado";
    }

    @GetMapping("/version")
    public String version() {
        return "build:" + BUILD;
    }

    /**
     * Endpoint publico de health/wake-up.
     * Use para:
     *   1. Acordar o pod do Render (free tier hiberna apos 15 min sem requests)
     *   2. Health check externo (UptimeRobot, cron, etc.)
     *
     * GET /api/ping  ->  200 {"status":"alive","ts":1234567890,"build":"..."}
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> body = new HashMap<>();
        body.put("status", "alive");
        body.put("ts", System.currentTimeMillis());
        body.put("build", BUILD);
        return ResponseEntity.ok(body);
    }
}
