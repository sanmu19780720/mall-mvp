package com.mall.mvp.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

/**
 * Real servlet-container integration test proving that {@code POST /api/user/register}
 * is reachable <em>without</em> a JWT. This deliberately runs over a random port with
 * {@link TestRestTemplate} rather than {@code MockMvc}: {@link com.mall.mvp.auth.JwtFilter}
 * is registered via a {@code FilterRegistrationBean}, which {@code @AutoConfigureMockMvc}
 * does not wire into its filter chain — so only a full-stack HTTP call actually exercises
 * the whitelist. A missing whitelist entry would surface here as 401 instead of 201.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RegisterNoTokenIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void registerWithoutTokenReturns201() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // No Authorization header on purpose.
        String body = "{\"username\":\"frank\",\"password\":\"password123\","
                + "\"phone\":\"13900007777\",\"nickname\":\"Frank\"}";

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/user/register", new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode())
                .as("registration must be public — no JWT required")
                .isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("\"username\":\"frank\"");
    }
}
