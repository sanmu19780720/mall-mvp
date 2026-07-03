package com.mall.mvp.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.mall.mvp.user.User;
import com.mall.mvp.user.UserRepository;

/**
 * Login and SMS-code business logic: account/password login (BCrypt), phone/SMS-code
 * login, and code dispatch. Translates domain failures into {@link AuthException}s
 * carrying the spec's error codes.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final SmsCodeStore smsCodeStore;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, SmsCodeStore smsCodeStore, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.smsCodeStore = smsCodeStore;
        this.jwtUtil = jwtUtil;
    }

    /** Result of a successful login: the JWT and its lifetime in seconds. */
    public record TokenResult(String token, long expiresIn) {
    }

    public TokenResult loginByPassword(String username, String password) {
        User user = userRepository.findByUsername(username)
                .filter(u -> passwordEncoder.matches(password, u.passwordHash()))
                // Same code whether the user is missing or the password is wrong, to
                // avoid leaking which one it was.
                .orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS"));
        return issueToken(user);
    }

    public TokenResult loginByPhone(String phone, String smsCode) {
        if (!smsCodeStore.verify(phone, smsCode)) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "INVALID_SMS_CODE");
        }
        // Registration is out of scope this iteration: the phone must map to a user.
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS"));
        return issueToken(user);
    }

    public void sendSmsCode(String phone) {
        try {
            String code = smsCodeStore.issue(phone);
            // No SMS gateway yet: surface the code in logs for local/dev verification.
            log.info("SMS verification code for {} is {}", phone, code);
        } catch (SmsCodeStore.TooManyRequestsException e) {
            throw new AuthException(HttpStatus.TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS");
        }
    }

    private TokenResult issueToken(User user) {
        String subject = user.username() != null ? user.username() : user.phone();
        String token = jwtUtil.generateToken(user.id(), subject);
        return new TokenResult(token, jwtUtil.getExpirationSeconds());
    }
}
