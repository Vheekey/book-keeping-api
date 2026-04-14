package com.calvary.finance.shared;

import com.calvary.finance.user.entity.User;
import com.calvary.finance.user.entity.UserAuthProvider;
import com.calvary.finance.user.enums.AuthProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(
                jwtService,
                "jwtSecret",
                "VGhpc0lzQVRlc3RTZWNyZXRLZXlGb3JKV1RUaGF0SXNMb25nRW5vdWdo"
        );
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 60_000L);
    }

    @Test
    void generateTokenCreatesValidTokenWithUserIdSubject() {
        User user = user();
        UserAuthProvider authProvider = authProvider();

        String token = jwtService.generateToken(user, authProvider);

        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo("42");
    }

    @Test
    void isTokenValidReturnsFalseForMalformedToken() {
        assertThat(jwtService.isTokenValid("not-a-jwt")).isFalse();
    }

    @Test
    void isTokenValidReturnsFalseForExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", -1L);

        String token = jwtService.generateToken(user(), authProvider());

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    private User user() {
        User user = new User();
        user.setId(42L);
        user.setEmail("john@example.com");
        user.setRoleCode("USER");
        return user;
    }

    private UserAuthProvider authProvider() {
        UserAuthProvider authProvider = new UserAuthProvider();
        authProvider.setProvider(AuthProvider.LOCAL);
        authProvider.setUsername("johndoe");
        return authProvider;
    }
}
