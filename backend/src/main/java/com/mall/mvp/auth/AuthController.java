package com.mall.mvp.auth;

import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public authentication endpoints:
 * <ul>
 *   <li>{@code POST /api/auth/sms-code} — send a verification code to a phone</li>
 *   <li>{@code POST /api/auth/login} — unified login (account/password OR phone/SMS-code)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /** 11-digit mainland China mobile number. */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public record SmsCodeRequest(String phone) {
    }

    public record LoginRequest(String username, String password, String phone, String smsCode) {
    }

    @PostMapping("/sms-code")
    public ResponseEntity<Map<String, Object>> sendSmsCode(@RequestBody(required = false) SmsCodeRequest request) {
        String phone = request == null ? null : request.phone();
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "INVALID_PHONE");
        }
        authService.sendSmsCode(phone);
        return ResponseEntity.ok(Map.of("sent", true));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody(required = false) LoginRequest request) {
        if (request == null) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST");
        }
        boolean passwordMode = hasText(request.username()) && hasText(request.password());
        boolean phoneMode = hasText(request.phone()) && hasText(request.smsCode());

        // The two modes are mutually exclusive: reject if neither is complete or both are given.
        if (passwordMode == phoneMode) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST");
        }

        AuthService.TokenResult result = passwordMode
                ? authService.loginByPassword(request.username(), request.password())
                : authService.loginByPhone(request.phone(), request.smsCode());

        return ResponseEntity.ok(Map.of("token", result.token(), "expiresIn", result.expiresIn()));
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, Object>> handleAuthException(AuthException e) {
        return ResponseEntity.status(e.getStatus()).body(Map.of("error", e.getError()));
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
