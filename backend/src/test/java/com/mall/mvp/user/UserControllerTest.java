package com.mall.mvp.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * End-to-end tests for {@code POST /api/user/register} over the full Spring context
 * (H2 + Flyway). The demo user seeded by {@code V2__user.sql}
 * ({@code username=demo}, {@code phone=13800138000}) is used to exercise the
 * uniqueness conflicts. Each success test uses a distinct username/phone.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registerSucceeds() throws Exception {
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"password123\","
                                + "\"phone\":\"13900001111\",\"nickname\":\"Alice\","
                                + "\"email\":\"alice@example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.nickname").value("Alice"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                // The response must never echo the password or its hash.
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void registerDuplicateUsernameReturns409() throws Exception {
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"demo\",\"password\":\"password123\","
                                + "\"phone\":\"13900002222\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("USERNAME_EXISTS"));
    }

    @Test
    void registerDuplicatePhoneReturns409() throws Exception {
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"bob\",\"password\":\"password123\","
                                + "\"phone\":\"13800138000\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("PHONE_EXISTS"));
    }

    @Test
    void registerMissingPasswordReturns400() throws Exception {
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"carol\",\"phone\":\"13900003333\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("PASSWORD_REQUIRED"));
    }

    @Test
    void registerMissingUsernameReturns400() throws Exception {
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"password123\",\"phone\":\"13900004444\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("USERNAME_REQUIRED"));
    }

    @Test
    void registerInvalidPhoneReturns400() throws Exception {
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"dave\",\"password\":\"password123\","
                                + "\"phone\":\"12345\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_PHONE"));
    }

    @Test
    void registerShortPasswordReturns400() throws Exception {
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"erin\",\"password\":\"123\","
                                + "\"phone\":\"13900005555\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("PASSWORD_TOO_SHORT"));
    }
}
