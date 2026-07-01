package com.mall.mvp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Mall MVP backend entry point (Stage 0 skeleton).
 *
 * Business domains live under sibling packages, each owned by one agent:
 *   com.mall.mvp.user / product / category / cart   -> Backend Agent A (100.64.21.7)
 *   com.mall.mvp.trade / admin                       -> Backend Agent B (100.95.162.7)
 *   com.mall.mvp.common / config / security          -> Air only (shared)
 *
 * This skeleton intentionally contains NO business endpoints.
 * The first dispatched task (add-health-endpoint) adds com.mall.mvp.health.
 */
@SpringBootApplication
public class MallMvpApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallMvpApplication.class, args);
    }
}
