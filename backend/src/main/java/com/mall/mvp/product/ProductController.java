package com.mall.mvp.product;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public product catalog endpoints (no authentication — whitelisted in
 * {@code JwtFilter.shouldNotFilter} for {@code /api/products}):
 * <ul>
 *   <li>{@code GET /api/products} — paged list of active products, optional category filter</li>
 *   <li>{@code GET /api/products/{id}} — single active product detail</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    /** Guardrail so a client cannot request an unbounded page. */
    private static final int MAX_PAGE_SIZE = 100;

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId) {
        if (page < 0) {
            throw new ProductException(HttpStatus.BAD_REQUEST, "INVALID_PAGE");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new ProductException(HttpStatus.BAD_REQUEST, "INVALID_SIZE");
        }

        long totalElements = productRepository.countActive(categoryId);
        List<Product> products = productRepository.findActive(categoryId, page, size);

        List<Map<String, Object>> content = new ArrayList<>(products.size());
        for (Product p : products) {
            content.add(toListItem(p));
        }

        int totalPages = (int) ((totalElements + size - 1) / size);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("content", content);
        body.put("totalElements", totalElements);
        body.put("totalPages", totalPages);
        body.put("page", page);
        body.put("size", size);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> detail(@PathVariable Long id) {
        Product product = productRepository.findActiveById(id)
                .orElseThrow(() -> new ProductException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND"));
        return ResponseEntity.ok(toDetail(product));
    }

    /** List projection: only the fields the browse view needs (no description/stock/status). */
    private static Map<String, Object> toListItem(Product p) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", p.id());
        item.put("name", p.name());
        item.put("mainImage", p.mainImage());
        item.put("price", p.price());
        item.put("salesCount", p.salesCount());
        item.put("categoryId", p.categoryId());
        return item;
    }

    /** Detail projection: full product plus its joined {@code categoryName}. */
    private static Map<String, Object> toDetail(Product p) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", p.id());
        item.put("name", p.name());
        item.put("mainImage", p.mainImage());
        item.put("description", p.description());
        item.put("price", p.price());
        item.put("stock", p.stock());
        item.put("salesCount", p.salesCount());
        item.put("categoryId", p.categoryId());
        item.put("categoryName", p.categoryName());
        return item;
    }

    @ExceptionHandler(ProductException.class)
    public ResponseEntity<Map<String, Object>> handleProductException(ProductException e) {
        return ResponseEntity.status(e.getStatus()).body(Map.of("error", e.getError()));
    }
}
