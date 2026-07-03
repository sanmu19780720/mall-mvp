package com.mall.mvp.auth;

import java.util.List;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Wiring for the auth domain: registers {@link JwtFilter} on {@code /api/*} and a
 * dev CORS filter so the static {@code frontend/login.html} can call the API when
 * opened standalone (file://).
 *
 * <p>Kept as a plain {@code @Configuration} (not a {@code WebMvcConfigurer}) and the
 * filters are wrapped in {@link FilterRegistrationBean}s rather than exposed as
 * {@code Filter} beans, so this config stays out of {@code @WebMvcTest} slices (e.g.
 * the health check) that don't provide {@link JwtUtil}.
 */
@Configuration
public class AuthConfig {

    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterRegistration(JwtUtil jwtUtil) {
        FilterRegistrationBean<JwtFilter> registration = new FilterRegistrationBean<>(new JwtFilter(jwtUtil));
        registration.addUrlPatterns("/api/*");
        registration.setName("jwtFilter");
        registration.setOrder(Ordered.LOWEST_PRECEDENCE);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration() {
        CorsConfiguration config = new CorsConfiguration();
        // Dev-only: token lives in the body / localStorage (no cookies), so "*" is safe here.
        config.addAllowedOrigin("*");
        config.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        config.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);

        FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>(new CorsFilter(source));
        registration.addUrlPatterns("/api/*");
        registration.setName("corsFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
