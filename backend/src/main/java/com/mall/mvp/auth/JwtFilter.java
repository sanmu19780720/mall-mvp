package com.mall.mvp.auth;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Guards protected {@code /api/**} endpoints by requiring a valid
 * {@code Authorization: Bearer <token>} header. Public paths — the auth endpoints,
 * the health check, CORS preflight, and any non-API (static) path — pass through.
 * Missing/invalid tokens get a 401 fallback.
 *
 * <p>Registered via {@code AuthConfig} (a {@link org.springframework.boot.web.servlet.FilterRegistrationBean})
 * rather than as a {@code @Component}, so it does not leak into web-slice
 * ({@code @WebMvcTest}) contexts that don't provide {@link JwtUtil}.
 */
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true; // let CORS preflight through
        }
        String path = request.getServletPath();
        if (!path.startsWith("/api/")) {
            return true; // static assets / non-API paths
        }
        return path.startsWith("/api/auth/") || path.equals("/api/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        String token = header != null && header.startsWith("Bearer ") ? header.substring(7) : null;
        if (token == null || !jwtUtil.isValid(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"UNAUTHORIZED\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
