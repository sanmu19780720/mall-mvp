package com.mall.mvp.product;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Full-stack tests for the public product catalog endpoints over a real servlet
 * container ({@code RANDOM_PORT} + {@link TestRestTemplate}), with H2 + Flyway seed data
 * from {@code V4__product.sql}.
 *
 * <p>Deliberately not {@code MockMvc}: {@link com.mall.mvp.auth.JwtFilter} is registered
 * via a {@code FilterRegistrationBean} that {@code @AutoConfigureMockMvc} does not wire
 * into its chain, so only a real HTTP call proves the {@code /api/products} whitelist
 * actually lets anonymous requests through (a missing entry would surface here as 401).
 *
 * <p>Seed data (active unless noted): id 1,2 → category 1 (服装); id 3 → category 2 (数码);
 * id 4 → category 2, <em>offline</em>; id 5 → category 3 (食品). Four active products total.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ProductControllerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private TestRestTemplate restTemplate;

    private JsonNode getJson(String url) throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return MAPPER.readTree(response.getBody());
    }

    @Test
    void listFirstPageReturnsPagedStructure() throws Exception {
        JsonNode body = getJson("/api/products?page=0&size=10");

        assertThat(body.get("totalElements").asLong()).isEqualTo(4);
        assertThat(body.get("totalPages").asInt()).isEqualTo(1);
        assertThat(body.get("page").asInt()).isEqualTo(0);
        assertThat(body.get("size").asInt()).isEqualTo(10);

        JsonNode content = body.get("content");
        assertThat(content.isArray()).isTrue();
        assertThat(content.size()).isEqualTo(4);

        JsonNode first = content.get(0);
        assertThat(first.has("id")).isTrue();
        assertThat(first.has("name")).isTrue();
        assertThat(first.has("mainImage")).isTrue();
        assertThat(first.has("price")).isTrue();
        assertThat(first.has("salesCount")).isTrue();
        assertThat(first.has("categoryId")).isTrue();

        // The offline product (id 4) must never appear in the active catalog.
        for (JsonNode item : content) {
            assertThat(item.get("id").asLong()).isNotEqualTo(4L);
        }
    }

    @Test
    void listPaginationRespectsSize() throws Exception {
        JsonNode body = getJson("/api/products?page=0&size=2");

        assertThat(body.get("totalElements").asLong()).isEqualTo(4);
        assertThat(body.get("totalPages").asInt()).isEqualTo(2);
        assertThat(body.get("content").size()).isEqualTo(2);
    }

    @Test
    void listFilteredByCategoryReturnsOnlyThatCategory() throws Exception {
        JsonNode body = getJson("/api/products?categoryId=1&page=0&size=10");

        assertThat(body.get("totalElements").asLong()).isEqualTo(2);
        JsonNode content = body.get("content");
        assertThat(content.size()).isEqualTo(2);
        for (JsonNode item : content) {
            assertThat(item.get("categoryId").asLong()).isEqualTo(1L);
        }
    }

    @Test
    void listForCategoryWithoutActiveProductsIsEmpty() throws Exception {
        JsonNode body = getJson("/api/products?categoryId=999&page=0&size=10");

        assertThat(body.get("totalElements").asLong()).isEqualTo(0);
        assertThat(body.get("content").isArray()).isTrue();
        assertThat(body.get("content").size()).isEqualTo(0);
        assertThat(body.get("totalPages").asInt()).isEqualTo(0);
    }

    @Test
    void listWithoutTokenReturns200() {
        // No Authorization header — proves the JwtFilter whitelist covers /api/products.
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/products", String.class);
        assertThat(response.getStatusCode())
                .as("product list must be public — no JWT required")
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void listSizeOverLimitReturns400() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/products?size=101", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void detailReturnsFullProduct() throws Exception {
        JsonNode body = getJson("/api/products/1");

        assertThat(body.get("id").asLong()).isEqualTo(1L);
        assertThat(body.get("name").asText()).isNotEmpty();
        assertThat(body.has("mainImage")).isTrue();
        assertThat(body.has("description")).isTrue();
        assertThat(body.has("price")).isTrue();
        assertThat(body.has("stock")).isTrue();
        assertThat(body.has("salesCount")).isTrue();
        assertThat(body.get("categoryId").asLong()).isEqualTo(1L);
        assertThat(body.get("categoryName").asText()).isEqualTo("服装");
    }

    @Test
    void detailNotFoundReturns404() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/products/99999", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("PRODUCT_NOT_FOUND");
    }

    @Test
    void detailForOfflineProductReturns404() {
        // id 4 exists but is offline — must be hidden from the public catalog.
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/products/4", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("PRODUCT_NOT_FOUND");
    }

    @Test
    void detailWithoutTokenReturns200() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/products/1", String.class);
        assertThat(response.getStatusCode())
                .as("product detail must be public — no JWT required")
                .isEqualTo(HttpStatus.OK);
    }
}
