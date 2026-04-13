package com.calvary.finance.user.service;

import com.calvary.finance.audit.AuditLogService;
import com.calvary.finance.shared.JwtService;
import com.calvary.finance.user.entity.User;
import com.calvary.finance.user.entity.UserAuthProvider;
import com.calvary.finance.user.enums.AuthProvider;
import com.calvary.finance.user.entity.Role;
import com.calvary.finance.user.repository.RoleRepository;
import com.calvary.finance.user.repository.UserAuthProviderRepository;
import com.calvary.finance.user.repository.UserRepository;
import com.calvary.finance.user.request.ChangeUserRoleRequest;
import com.calvary.finance.user.request.CreateNewUserRequest;
import com.calvary.finance.user.request.UpdateUserRequest;
import com.calvary.finance.user.response.AuthResponse;
import com.calvary.finance.user.response.UserResponse;
import com.calvary.finance.user.response.UsersResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserAuthProviderRepository userAuthProviderRepository;
    private final RoleRepository roleRepository;
    private final AuditLogService auditLogService;
    private final JwtService jwtService;
    private static final String ENTITY_NAME = "User";

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserAuthProviderRepository userAuthProviderRepository,
            RoleRepository roleRepository,
            AuditLogService auditLogService,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userAuthProviderRepository = userAuthProviderRepository;
        this.roleRepository = roleRepository;
        this.auditLogService = auditLogService;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse createUser(CreateNewUserRequest request) {
        UserResponse userResponse = createUserResponse(request);
        return getAuthResponse(userResponse.getId());
    }

    @Transactional
    public UsersResponse createUserByAdmin(CreateNewUserRequest request) {
        UserResponse userResponse = createUserResponse(request);

        return new UsersResponse()
                .setMessage("User created")
                .setUsers(List.of(userResponse))
                .setPageNumber(0)
                .setPageSize(1)
                .setTotalElements(1L)
                .setTotalPages(1);
    }

    private UserResponse createUserResponse(CreateNewUserRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        Role userRole = getRoleByCode("USER");
        user.setRoleId(userRole.getId());
        user.setRoleCode(toRoleCode(userRole.getName()));
        user.setIsActive(true);

        User newUser = userRepository.save(user);

        UserAuthProvider userAuthProvider = new UserAuthProvider();
        userAuthProvider.setUserId(newUser.getId());
        userAuthProvider.setPassword(encodePassword(request.getPassword()));
        userAuthProvider.setProvider(AuthProvider.valueOf("LOCAL"));
        userAuthProvider.setUsername(request.getUsername());
        userAuthProvider.setProviderSubject(null);

        UserAuthProvider authProvider = userAuthProviderRepository.save(userAuthProvider);

        auditLogService.log(
                "NEW_USER_CREATED",
                ENTITY_NAME,
                String.valueOf(newUser.getId()),
                buildAuditDetails(user, authProvider)

        );
        return getUserResponse(newUser, authProvider);
    }

    public UsersResponse getUsers(int pageNumber, int pageSize, String search) {
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, Sort.by("id").descending());
        String normalizedSearch = search == null ? "" : search.trim();
        Page<User> page = normalizedSearch.isEmpty()
                ? userRepository.findAll(pageRequest)
                : userRepository.searchUsers(normalizedSearch.toLowerCase(), pageRequest);
        List<UserResponse> users = page.getContent()
                .stream()
                .map(this::toUserResponse)
                .toList();

        return new UsersResponse()
                .setMessage("Users retrieved")
                .setUsers(users)
                .setPageNumber(pageNumber)
                .setPageSize(pageSize)
                .setTotalElements(page.getTotalElements())
                .setTotalPages(page.getTotalPages());
    }

    public UsersResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return new UsersResponse()
                .setMessage("User retrieved")
                .setUsers(List.of(toUserResponse(user)))
                .setPageNumber(0)
                .setPageSize(1)
                .setTotalElements(1L)
                .setTotalPages(1);
    }

    @Transactional
    public UsersResponse updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        UserAuthProvider userAuthProvider = userAuthProviderRepository
                .findByUserIdAndProvider(userId, AuthProvider.LOCAL)
                .orElseThrow(() -> new RuntimeException("Local login is not configured for this user"));

        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (!userAuthProvider.getUsername().equalsIgnoreCase(request.getUsername())
                && userAuthProviderRepository.existsByProviderAndUsername(AuthProvider.LOCAL, request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        userAuthProvider.setUsername(request.getUsername());

        User savedUser = userRepository.save(user);
        UserAuthProvider savedAuthProvider = userAuthProviderRepository.save(userAuthProvider);

        auditLogService.log(
                "USER_UPDATED",
                ENTITY_NAME,
                String.valueOf(savedUser.getId()),
                buildAuditDetails(savedUser, savedAuthProvider)
        );

        return new UsersResponse()
                .setMessage("User updated")
                .setUsers(List.of(getUserResponse(savedUser, savedAuthProvider)))
                .setPageNumber(0)
                .setPageSize(1)
                .setTotalElements(1L)
                .setTotalPages(1);
    }

    @Transactional
    public UsersResponse changeUserRole(Long userId, ChangeUserRoleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Role role = getRoleByCode(request.getRole());

        user.setRoleId(role.getId());
        user.setRoleCode(toRoleCode(role.getName()));
        User savedUser = userRepository.save(user);

        return new UsersResponse()
                .setMessage("User role updated")
                .setUsers(List.of(toUserResponse(savedUser)))
                .setPageNumber(0)
                .setPageSize(1)
                .setTotalElements(1L)
                .setTotalPages(1);
    }

    @Transactional
    public UsersResponse changeUserActiveStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        UserAuthProvider userAuthProvider = getLocalUserAuthProvider(user.getId()).orElse(null);

        user.setIsActive(!Boolean.TRUE.equals(user.getIsActive()));
        User savedUser = userRepository.save(user);

        auditLogService.log(
                "USER_STATUS_CHANGED",
                ENTITY_NAME,
                String.valueOf(savedUser.getId()),
                buildAuditDetails(savedUser, userAuthProvider)
        );

        return new UsersResponse()
                .setMessage("User status updated")
                .setUsers(List.of(getUserResponse(savedUser, userAuthProvider)))
                .setPageNumber(0)
                .setPageSize(1)
                .setTotalElements(1L)
                .setTotalPages(1);
    }

    @Transactional
    public UsersResponse deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        UserAuthProvider userAuthProvider = getLocalUserAuthProvider(user.getId()).orElse(null);
        UserResponse deletedUser = getUserResponse(user, userAuthProvider);

        auditLogService.log(
                "USER_DELETED",
                ENTITY_NAME,
                String.valueOf(user.getId()),
                buildAuditDetails(user, userAuthProvider)
        );

        userRepository.delete(user);

        return new UsersResponse()
                .setMessage("User deleted")
                .setUsers(List.of(deletedUser))
                .setPageNumber(0)
                .setPageSize(1)
                .setTotalElements(1L)
                .setTotalPages(1);
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    private UserResponse toUserResponse(User user) {
        UserAuthProvider userAuthProvider = getLocalUserAuthProvider(user.getId()).orElse(null);
        return getUserResponse(user, userAuthProvider);
    }

    private Optional<UserAuthProvider> getLocalUserAuthProvider(Long userId) {
        return userAuthProviderRepository.findByUserIdAndProvider(userId, AuthProvider.LOCAL);
    }

    private UserResponse getUserResponse(User user, UserAuthProvider userAuthProvider) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setName(user.getName());
        userResponse.setEmail(user.getEmail());
        userResponse.setRoleId(user.getRoleId());
        userResponse.setRoleName(resolveUserRoleCode(user));
        userResponse.setUsername(userAuthProvider == null ? null : userAuthProvider.getUsername());
        userResponse.setIsActive(user.getIsActive());

        return userResponse;
    }

    private Role getRoleByCode(String roleCode) {
        return roleRepository.findByNameIgnoreCase(toRoleName(roleCode))
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));
    }

    private String resolveUserRoleCode(User user) {
        if (user.getRoleCode() != null && !user.getRoleCode().isBlank()) {
            return toRoleCode(user.getRoleCode());
        }

        return roleRepository.findById(user.getRoleId())
                .map(role -> toRoleCode(role.getName()))
                .orElse(null);
    }

    private String toRoleName(String roleCode) {
        return toRoleCode(roleCode).toLowerCase();
    }

    private String toRoleCode(String roleCode) {
        return roleCode == null ? "" : roleCode.trim().toUpperCase();
    }

    private Map<String, Object> buildAuditDetails(User user, UserAuthProvider userAuthProvider) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("userId", user.getId());
        details.put("name", user.getName());
        details.put("email", user.getEmail());
        details.put("roleId", user.getRoleId());
        details.put("provider", userAuthProvider == null ? null : userAuthProvider.getProvider());
        details.put("providerSubject", userAuthProvider == null ? null : userAuthProvider.getProviderSubject());
        details.put("isActive", user.getIsActive());

        return details;
    }

    public AuthResponse loginWithEmail(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return loginWithUser(user, password, "Invalid email/password");
    }

    public AuthResponse loginWithUsername(String username, String password) {
        UserAuthProvider userAuthProvider = userAuthProviderRepository
                .findByProviderAndUsername(AuthProvider.LOCAL, username)
                .orElseThrow(() -> new RuntimeException("Invalid username/password"));

        User user = userRepository.findById(userAuthProvider.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return loginWithUser(user, userAuthProvider, password, "Invalid username/password");
    }

    private AuthResponse loginWithUser(User user, String password, String invalidCredentialsMessage) {
        UserAuthProvider userAuthProvider = userAuthProviderRepository
                .findByUserIdAndProvider(user.getId(), AuthProvider.LOCAL)
                .orElseThrow(() -> new RuntimeException("Local login is not configured for this user"));

        return loginWithUser(user, userAuthProvider, password, invalidCredentialsMessage);
    }

    private AuthResponse loginWithUser(
            User user,
            UserAuthProvider userAuthProvider,
            String password,
            String invalidCredentialsMessage
    ) {
        if (!passwordEncoder.matches(password, userAuthProvider.getPassword())) {
            throw new RuntimeException(invalidCredentialsMessage);
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new RuntimeException("Inactive account");
        }

        return getAuthResponse(user, userAuthProvider);
    }


    private AuthResponse getAuthResponse(User user, UserAuthProvider userAuthProvider) {
        String jwtToken = jwtService.generateToken(user, userAuthProvider);
        UserResponse userResponse = getUserResponse(user, userAuthProvider);

        return new AuthResponse().setUser(userResponse).setToken(jwtToken);
    }

    private AuthResponse getAuthResponse(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        UserAuthProvider userAuthProvider = getLocalUserAuthProvider(userId)
                .orElseThrow(() -> new RuntimeException("Local login is not configured for this user"));

        return getAuthResponse(user, userAuthProvider);
    }
}
