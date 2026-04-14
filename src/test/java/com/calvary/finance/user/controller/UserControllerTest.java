package com.calvary.finance.user.controller;

import com.calvary.finance.shared.JwtService;
import com.calvary.finance.user.repository.UserAuthProviderRepository;
import com.calvary.finance.user.repository.UserRepository;
import com.calvary.finance.user.request.ChangeUserRoleRequest;
import com.calvary.finance.user.request.CreateNewUserRequest;
import com.calvary.finance.user.request.UpdateUserRequest;
import com.calvary.finance.user.response.AuthResponse;
import com.calvary.finance.user.response.UserResponse;
import com.calvary.finance.user.response.UsersResponse;
import com.calvary.finance.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserAuthProviderRepository userAuthProviderRepository;

    @Test
    void createReturnsAuthResponse() throws Exception {
        when(userService.createUser(any(CreateNewUserRequest.class)))
                .thenReturn(authResponse());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserPayload()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.user.email").value("john@example.com"));
    }

    @Test
    void createValidatesRequestBody() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "email": "bad-email",
                                  "password": "",
                                  "username": "john"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void getUsersPassesPagingAndSearchParametersToService() throws Exception {
        when(userService.getUsers(1, 20, "john")).thenReturn(usersResponse("Users retrieved"));

        mockMvc.perform(get("/users")
                        .param("pageNumber", "1")
                        .param("pageSize", "20")
                        .param("search", "john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Users retrieved"));

        verify(userService).getUsers(1, 20, "john");
    }

    @Test
    void updateUserPassesPathVariableAndBodyToService() throws Exception {
        when(userService.updateUser(eq(42L), any(UpdateUserRequest.class)))
                .thenReturn(usersResponse("User updated"));

        mockMvc.perform(put("/users/{userId}", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "John Doe",
                                  "email": "john@example.com",
                                  "username": "johndoe"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User updated"));

        verify(userService).updateUser(eq(42L), any(UpdateUserRequest.class));
    }

    @Test
    void changeUserRolePassesPathVariableAndBodyToService() throws Exception {
        when(userService.changeUserRole(eq(42L), any(ChangeUserRoleRequest.class)))
                .thenReturn(usersResponse("User role updated"));

        mockMvc.perform(post("/users/{userId}/role", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "ADMIN"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User role updated"));
    }

    @Test
    void loginWithUsernameUsesUsernameBranch() throws Exception {
        when(userService.loginWithUsername("johndoe", "secret")).thenReturn(authResponse());

        mockMvc.perform(post("/users/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "johndoe",
                                  "password": "secret"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));

        verify(userService).loginWithUsername("johndoe", "secret");
    }

    @Test
    void loginWithoutUsernameUsesEmailBranch() throws Exception {
        when(userService.loginWithEmail("john@example.com", "secret")).thenReturn(authResponse());

        mockMvc.perform(post("/users/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "john@example.com",
                                  "password": "secret"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));

        verify(userService).loginWithEmail("john@example.com", "secret");
    }

    private String createUserPayload() {
        return """
                {
                  "name": "John Doe",
                  "email": "john@example.com",
                  "password": "secret",
                  "username": "johndoe"
                }
                """;
    }

    private AuthResponse authResponse() {
        return new AuthResponse()
                .setToken("jwt-token")
                .setUser(userResponse());
    }

    private UsersResponse usersResponse(String message) {
        return new UsersResponse()
                .setMessage(message)
                .setUsers(List.of(userResponse()))
                .setPageNumber(0)
                .setPageSize(1)
                .setTotalElements(1L)
                .setTotalPages(1);
    }

    private UserResponse userResponse() {
        UserResponse user = new UserResponse();
        user.setId(42L);
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setUsername("johndoe");
        user.setRoleName("USER");
        user.setIsActive(true);
        return user;
    }
}
