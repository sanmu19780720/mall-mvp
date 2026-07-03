package com.mall.mvp.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Direct unit tests for {@link JwtFilter}. Exercises the filter in isolation (no
 * servlet container / MockMvc) since it is registered via a {@code FilterRegistrationBean}.
 */
class JwtFilterTest {

    private final JwtUtil jwtUtil =
            new JwtUtil("mall-mvp-dev-secret-please-override-in-prod-0123456789abcdef", 86400);
    private final JwtFilter filter = new JwtFilter(jwtUtil);

    private static MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest req = new MockHttpServletRequest(method, path);
        req.setServletPath(path);
        return req;
    }

    @Test
    void protectedPathWithoutTokenIsRejected() throws Exception {
        MockHttpServletRequest req = request("GET", "/api/orders");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, res.getStatus());
        assertNull(chain.getRequest(), "chain must not proceed without a valid token");
    }

    @Test
    void protectedPathWithInvalidTokenIsRejected() throws Exception {
        MockHttpServletRequest req = request("GET", "/api/orders");
        req.addHeader("Authorization", "Bearer not-a-real-token");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, res.getStatus());
        assertNull(chain.getRequest());
    }

    @Test
    void protectedPathWithValidTokenProceeds() throws Exception {
        String token = jwtUtil.generateToken(1L, "demo");
        MockHttpServletRequest req = request("GET", "/api/orders");
        req.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertEquals(HttpServletResponse.SC_OK, res.getStatus());
        assertNotNull(chain.getRequest(), "chain should proceed with a valid token");
    }

    @Test
    void authPathIsPublic() throws Exception {
        MockHttpServletRequest req = request("POST", "/api/auth/login");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertNotNull(chain.getRequest(), "auth endpoints must bypass the token check");
    }

    @Test
    void registerPathIsPublic() throws Exception {
        MockHttpServletRequest req = request("POST", "/api/user/register");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertNotNull(chain.getRequest(), "registration endpoint must bypass the token check");
    }

    @Test
    void healthPathIsPublic() throws Exception {
        MockHttpServletRequest req = request("GET", "/api/health");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertNotNull(chain.getRequest(), "health endpoint must bypass the token check");
    }
}
