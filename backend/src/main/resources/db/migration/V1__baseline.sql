-- Stage 0 baseline migration.
-- Establishes the Flyway migration chain. Business tables are added by later
-- migrations (V2, V3, ...) owned by Air or explicitly authorized agents.
-- Uses ANSI-standard SQL so it runs on both MySQL (prod) and H2 (test/CI).

CREATE TABLE IF NOT EXISTS app_meta (
    id          BIGINT       NOT NULL,
    meta_key    VARCHAR(64)  NOT NULL,
    meta_value  VARCHAR(255),
    CONSTRAINT pk_app_meta PRIMARY KEY (id)
);

INSERT INTO app_meta (id, meta_key, meta_value) VALUES (1, 'schema_stage', 'stage-0-baseline');
