package com.calvary.finance.shared;

import com.calvary.finance.user.entity.User;
import com.calvary.finance.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterSetsAuthenticationForValidBearerTokenAndActiveUser() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userRepository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer valid-token");
        User user = new User();
        user.setId(42L);
        user.setRoleCode("ADMIN");
        user.setIsActive(true);

        when(jwtService.isTokenValid("valid-token")).thenReturn(true);
        when(jwtService.extractUserId("valid-token")).thenReturn("42");
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));

        AtomicReference<Authentication> authenticationInChain = new AtomicReference<>();
        FilterChain filterChain = (servletRequest, servletResponse) ->
                authenticationInChain.set(SecurityContextHolder.getContext().getAuthentication());

        filter.doFilter(request, response, filterChain);

        Authentication authentication = authenticationInChain.get();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(42L);
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void doFilterLeavesAuthenticationEmptyWhenTokenIsInvalid() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userRepository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalid-token");

        when(jwtService.isTokenValid("invalid-token")).thenReturn(false);

        AtomicReference<Authentication> authenticationInChain = new AtomicReference<>();
        FilterChain filterChain = (servletRequest, servletResponse) ->
                authenticationInChain.set(SecurityContextHolder.getContext().getAuthentication());

        filter.doFilter(request, response, filterChain);

        assertThat(authenticationInChain.get()).isNull();
        verify(jwtService, never()).extractUserId("invalid-token");
        verify(userRepository, never()).findById(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void doFilterLeavesAuthenticationEmptyWhenUserIsInactive() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userRepository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer valid-token");
        User user = new User();
        user.setId(42L);
        user.setRoleCode("ADMIN");
        user.setIsActive(false);

        when(jwtService.isTokenValid("valid-token")).thenReturn(true);
        when(jwtService.extractUserId("valid-token")).thenReturn("42");
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));

        AtomicReference<Authentication> authenticationInChain = new AtomicReference<>();
        FilterChain filterChain = (servletRequest, servletResponse) ->
                authenticationInChain.set(SecurityContextHolder.getContext().getAuthentication());

        filter.doFilter(request, response, filterChain);

        assertThat(authenticationInChain.get()).isNull();
    }
}
