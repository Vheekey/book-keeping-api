package com.calvary.finance.user.controller;

import com.calvary.finance.user.request.ChangeUserRoleRequest;
import com.calvary.finance.user.response.AuthResponse;
import com.calvary.finance.user.service.UserService;
import com.calvary.finance.user.request.CreateNewUserRequest;
import com.calvary.finance.user.request.UpdateUserRequest;
import com.calvary.finance.user.response.UsersResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<AuthResponse> create(@Valid @RequestBody CreateNewUserRequest createNewUserRequest) {
        AuthResponse response = userService.createUser(createNewUserRequest);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('SADMIN', 'ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<UsersResponse> createByAdmin(@Valid @RequestBody CreateNewUserRequest createNewUserRequest) {
        UsersResponse response = userService.createUserByAdmin(createNewUserRequest);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SADMIN')")
    @GetMapping
    public ResponseEntity<UsersResponse> getUsers(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String search
    ) {
        UsersResponse response = userService.getUsers(pageNumber, pageSize, search);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SADMIN')")
    @GetMapping("/{userId}")
    public ResponseEntity<UsersResponse> getUser(@PathVariable Long userId) {
        UsersResponse response = userService.getUser(userId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SADMIN')")
    @PutMapping("/{userId}")
    public ResponseEntity<UsersResponse> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest updateUserRequest
    ) {
        UsersResponse response = userService.updateUser(userId, updateUserRequest);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SADMIN')")
    @PutMapping("/{userId}/change-status")
    public ResponseEntity<UsersResponse> changeUserStatus(@PathVariable Long userId) {
        UsersResponse response = userService.changeUserActiveStatus(userId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SADMIN')")
    @PostMapping("/{userId}/role")
    public ResponseEntity<UsersResponse> changeUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody ChangeUserRoleRequest request
    ) {
        UsersResponse response = userService.changeUserRole(userId, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SADMIN')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<UsersResponse> deleteUser(@PathVariable Long userId) {
        UsersResponse response = userService.deleteUser(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponse> login(@RequestBody Map<String, String> request){
        String username = request.get("username");
        String email = request.get("email");
        String password = request.get("password");
        AuthResponse response;
        if (username != null && !username.isEmpty()){
            response = userService.loginWithUsername(username, password);
        }
        else{
            response = userService.loginWithEmail(email, password);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return ResponseEntity.noContent().build();
    }
}
