-- add-login-page change: user identity table.
-- Backs account/password + phone/SMS login (see openspec/changes/add-login-page).
-- ANSI-standard SQL so it runs on both MySQL (prod) and H2 (test/CI), matching V1.

CREATE TABLE IF NOT EXISTS users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    username      VARCHAR(64),
    phone         VARCHAR(20),
    password_hash VARCHAR(100) NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_phone UNIQUE (phone)
);

-- Demo account for local/dev verification (frontend manual test + integration tests).
-- Password is "password123" (BCrypt, cost 10). Replace/remove for real deployments.
INSERT INTO users (username, phone, password_hash) VALUES
    ('demo', '13800138000', '$2a$10$Ks/sVksYevuy0BISgljzuelJjL/f4Cy.duk3qCEysEznDcdRfJD5i');
