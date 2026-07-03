package com.mall.mvp.auth;

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
 * End-to-end tests for the auth endpoints over the full Spring context (H2 + Flyway).
 * Relies on the demo user seeded by {@code V2__user.sql}:
 * {@code username=demo}, {@code password=password123}, {@code phone=13800138000}.
 * Each test uses a distinct phone to stay independent of the singleton {@link SmsCodeStore}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SmsCodeStore smsCodeStore;

    // ---- account / password login ------------------------------------------

    @Test
    void passwordLoginSucceeds() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"demo\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.expiresIn").value(86400));
    }

    @Test
    void passwordLoginWrongPasswordReturns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"demo\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
    }

    @Test
    void passwordLoginUnknownUserReturns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ghost\",\"password\":\"password123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
    }

    @Test
    void loginWithNoModeReturns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
    }

    // ---- phone / SMS-code login --------------------------------------------

    @Test
    void phoneLoginSucceedsWithCorrectCode() throws Exception {
        String phone = "13800138000"; // seeded demo user
        mockMvc.perform(post("/api/auth/sms-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"" + phone + "\"}"))
                .andExpect(status().isOk());

        String code = smsCodeStore.peek(phone);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"" + phone + "\",\"smsCode\":\"" + code + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.expiresIn").value(86400));
    }

    @Test
    void phoneLoginWrongCodeReturns401() throws Exception {
        String phone = "13900139000";
        mockMvc.perform(post("/api/auth/sms-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"" + phone + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"" + phone + "\",\"smsCode\":\"000000\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_SMS_CODE"));
    }

    @Test
    void phoneLoginNoCodeIssuedReturns401() throws Exception {
        // Never requested a code for this phone -> nothing to verify against.
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13500135000\",\"smsCode\":\"123456\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_SMS_CODE"));
    }

    // ---- send SMS code ------------------------------------------------------

    @Test
    void sendSmsCodeSucceeds() throws Exception {
        mockMvc.perform(post("/api/auth/sms-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13700137000\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sent").value(true));
    }

    @Test
    void sendSmsCodeInvalidPhoneReturns400() throws Exception {
        mockMvc.perform(post("/api/auth/sms-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"12345\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_PHONE"));
    }

    @Test
    void sendSmsCodeTwiceReturns429() throws Exception {
        String phone = "13600136000";
        mockMvc.perform(post("/api/auth/sms-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"" + phone + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/sms-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"" + phone + "\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("TOO_MANY_REQUESTS"));
    }
}
