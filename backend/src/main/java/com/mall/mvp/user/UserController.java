package com.mall.mvp.user;

import java.util.LinkedHashMap;
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
 * User endpoints. Currently public self-service registration:
 * {@code POST /api/user/register}.
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    /** 11-digit mainland China mobile number (same rule as the auth domain). */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    /** Deliberately permissive: one {@code @}, non-empty local and domain parts. */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int MIN_PASSWORD_LENGTH = 6;

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public record RegisterRequest(
            String username, String password, String phone, String nickname, String email) {
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @RequestBody(required = false) RegisterRequest request) {
        if (request == null) {
            throw new UserException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST");
        }

        String username = trimToNull(request.username());
        String password = request.password();
        String phone = trimToNull(request.phone());
        String nickname = trimToNull(request.nickname());
        String email = trimToNull(request.email());

        if (username == null) {
            throw new UserException(HttpStatus.BAD_REQUEST, "USERNAME_REQUIRED");
        }
        if (password == null || password.isBlank()) {
            throw new UserException(HttpStatus.BAD_REQUEST, "PASSWORD_REQUIRED");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new UserException(HttpStatus.BAD_REQUEST, "PASSWORD_TOO_SHORT");
        }
        if (phone == null) {
            throw new UserException(HttpStatus.BAD_REQUEST, "PHONE_REQUIRED");
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new UserException(HttpStatus.BAD_REQUEST, "INVALID_PHONE");
        }
        if (email != null && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new UserException(HttpStatus.BAD_REQUEST, "INVALID_EMAIL");
        }

        User user = userService.register(username, password, phone, nickname, email);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", user.id());
        body.put("username", user.username());
        body.put("nickname", user.nickname());
        body.put("status", user.status());
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<Map<String, Object>> handleUserException(UserException e) {
        return ResponseEntity.status(e.getStatus()).body(Map.of("error", e.getError()));
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
