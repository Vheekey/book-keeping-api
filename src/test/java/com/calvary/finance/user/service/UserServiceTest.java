package com.calvary.finance.user.service;

import com.calvary.finance.audit.AuditLogService;
import com.calvary.finance.shared.JwtService;
import com.calvary.finance.user.entity.Role;
import com.calvary.finance.user.entity.User;
import com.calvary.finance.user.entity.UserAuthProvider;
import com.calvary.finance.user.enums.AuthProvider;
import com.calvary.finance.user.repository.RoleRepository;
import com.calvary.finance.user.repository.UserAuthProviderRepository;
import com.calvary.finance.user.repository.UserRepository;
import com.calvary.finance.user.request.ChangeUserRoleRequest;
import com.calvary.finance.user.request.CreateNewUserRequest;
import com.calvary.finance.user.request.UpdateUserRequest;
import com.calvary.finance.user.response.AuthResponse;
import com.calvary.finance.user.response.UsersResponse;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserAuthProviderRepository userAuthProviderRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @Test
    void createUserCreatesLocalAuthProviderAndReturnsToken() {
        CreateNewUserRequest request = createUserRequest();
        Role userRole = role(7L, "user");

        when(roleRepository.findByNameIgnoreCase("user")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(42L);
            return user;
        });
        when(userAuthProviderRepository.save(any(UserAuthProvider.class))).thenAnswer(invocation -> {
            UserAuthProvider authProvider = invocation.getArgument(0);
            authProvider.setId(100L);
            return authProvider;
        });
        when(userRepository.findById(42L)).thenAnswer(invocation -> Optional.of(user("John Doe", "john@example.com", 42L, 7L, "USER", true)));
        when(userAuthProviderRepository.findByUserIdAndProvider(42L, AuthProvider.LOCAL)).thenAnswer(invocation -> {
            UserAuthProvider authProvider = localAuthProvider(42L, "johndoe", "encoded-secret");
            return Optional.of(authProvider);
        });
        when(jwtService.generateToken(any(User.class), any(UserAuthProvider.class))).thenReturn("jwt-token");

        AuthResponse response = userService.createUser(request);

        ArgumentCaptor<UserAuthProvider> authProviderCaptor = ArgumentCaptor.forClass(UserAuthProvider.class);
        verify(userAuthProviderRepository).save(authProviderCaptor.capture());
        UserAuthProvider savedAuthProvider = authProviderCaptor.getValue();

        assertThat(savedAuthProvider.getUserId()).isEqualTo(42L);
        assertThat(savedAuthProvider.getPassword()).isEqualTo("encoded-secret");
        assertThat(savedAuthProvider.getProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(savedAuthProvider.getUsername()).isEqualTo("johndoe");
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUser().getId()).isEqualTo(42L);
        assertThat(response.getUser().getRoleName()).isEqualTo("USER");

        verify(auditLogService).log(
                eq("NEW_USER_CREATED"),
                eq("User"),
                eq("42"),
                any(Map.class)
        );
    }

    @Test
    void loginWithUsernameReturnsTokenWhenPasswordMatchesAndUserIsActive() {
        UserAuthProvider authProvider = localAuthProvider(42L, "johndoe", "encoded-secret");
        User user = user("John Doe", "john@example.com", 42L, 7L, "USER", true);

        when(userAuthProviderRepository.findByProviderAndUsername(AuthProvider.LOCAL, "johndoe"))
                .thenReturn(Optional.of(authProvider));
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "encoded-secret")).thenReturn(true);
        when(jwtService.generateToken(user, authProvider)).thenReturn("jwt-token");

        AuthResponse response = userService.loginWithUsername("johndoe", "secret");

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUser().getEmail()).isEqualTo("john@example.com");
        assertThat(response.getUser().getUsername()).isEqualTo("johndoe");
    }

    @Test
    void loginWithUsernameRejectsInvalidPassword() {
        UserAuthProvider authProvider = localAuthProvider(42L, "johndoe", "encoded-secret");
        User user = user("John Doe", "john@example.com", 42L, 7L, "USER", true);

        when(userAuthProviderRepository.findByProviderAndUsername(AuthProvider.LOCAL, "johndoe"))
                .thenReturn(Optional.of(authProvider));
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad-secret", "encoded-secret")).thenReturn(false);

        assertThatThrownBy(() -> userService.loginWithUsername("johndoe", "bad-secret"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid username/password");
    }

    @Test
    void loginWithEmailRejectsInactiveAccount() {
        User user = user("John Doe", "john@example.com", 42L, 7L, "USER", false);
        UserAuthProvider authProvider = localAuthProvider(42L, "johndoe", "encoded-secret");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(userAuthProviderRepository.findByUserIdAndProvider(42L, AuthProvider.LOCAL))
                .thenReturn(Optional.of(authProvider));
        when(passwordEncoder.matches("secret", "encoded-secret")).thenReturn(true);

        assertThatThrownBy(() -> userService.loginWithEmail("john@example.com", "secret"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Inactive account");
    }

    @Test
    void updateUserRejectsDuplicateEmail() {
        User user = user("John Doe", "john@example.com", 42L, 7L, "USER", true);
        UserAuthProvider authProvider = localAuthProvider(42L, "johndoe", "encoded-secret");
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("John Doe");
        request.setEmail("taken@example.com");
        request.setUsername("johndoe");

        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(userAuthProviderRepository.findByUserIdAndProvider(42L, AuthProvider.LOCAL))
                .thenReturn(Optional.of(authProvider));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(42L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email already exists");
    }

    @Test
    void changeUserRoleUpdatesRoleCodeFromRequestedRole() {
        User user = user("John Doe", "john@example.com", 42L, 7L, "USER", true);
        Role adminRole = role(8L, "admin");
        ChangeUserRoleRequest request = new ChangeUserRoleRequest();
        request.setRole("ADMIN");

        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(roleRepository.findByNameIgnoreCase("admin")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(user)).thenReturn(user);

        UsersResponse response = userService.changeUserRole(42L, request);

        assertThat(user.getRoleId()).isEqualTo(8L);
        assertThat(user.getRoleCode()).isEqualTo("ADMIN");
        assertThat(response.getMessage()).isEqualTo("User role updated");
        assertThat(response.getUsers().get(0).getRoleName()).isEqualTo("ADMIN");
    }

    @Test
    void getUserThrowsWhenMissing() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(404L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");
    }

    private CreateNewUserRequest createUserRequest() {
        CreateNewUserRequest request = new CreateNewUserRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setUsername("johndoe");
        request.setPassword("secret");
        return request;
    }

    private Role role(Long id, String name) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        return role;
    }

    private User user(String name, String email, Long id, Long roleId, String roleCode, boolean active) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setRoleId(roleId);
        user.setRoleCode(roleCode);
        user.setIsActive(active);
        return user;
    }

    private UserAuthProvider localAuthProvider(Long userId, String username, String password) {
        UserAuthProvider authProvider = new UserAuthProvider();
        authProvider.setUserId(userId);
        authProvider.setProvider(AuthProvider.LOCAL);
        authProvider.setUsername(username);
        authProvider.setPassword(password);
        return authProvider;
    }
}
