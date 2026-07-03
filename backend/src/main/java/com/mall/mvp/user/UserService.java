package com.mall.mvp.user;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * User-domain business logic. Currently self-service registration: uniqueness checks,
 * BCrypt password hashing, and persistence. Uniqueness failures surface as
 * {@link UserException}s carrying the spec's error codes.
 */
@Service
public class UserService {

    /** New accounts start active. */
    private static final String STATUS_ACTIVE = "ACTIVE";

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Registers a new user. The caller is expected to have validated presence/format;
     * this method enforces cross-row uniqueness (username, phone, and email when given)
     * before hashing the password and inserting the row.
     *
     * @return the persisted user (with DB-assigned {@code id} and {@code createdAt})
     */
    public User register(String username, String password, String phone, String nickname, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new UserException(HttpStatus.CONFLICT, "USERNAME_EXISTS");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new UserException(HttpStatus.CONFLICT, "PHONE_EXISTS");
        }
        if (email != null && userRepository.existsByEmail(email)) {
            throw new UserException(HttpStatus.CONFLICT, "EMAIL_EXISTS");
        }

        User toSave = new User(
                null, username, phone, passwordEncoder.encode(password),
                nickname, email, STATUS_ACTIVE, null);
        return userRepository.save(toSave);
    }
}
