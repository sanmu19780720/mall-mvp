package com.mall.mvp.health;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal liveness endpoint for the add-health-endpoint change.
 *
 * Exposes an unauthenticated {@code GET /api/health} that always reports
 * {@code {"status":"ok"}} with HTTP 200. No Service / Repository / DB
 * dependency by design (see openspec/changes/add-health-endpoint/design.md).
 */
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
