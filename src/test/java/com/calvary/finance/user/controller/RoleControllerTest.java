package com.calvary.finance.user.controller;

import com.calvary.finance.shared.JwtService;
import com.calvary.finance.user.entity.Role;
import com.calvary.finance.user.repository.UserRepository;
import com.calvary.finance.user.request.CreateNewRoleRequest;
import com.calvary.finance.user.request.UpdateRoleRequest;
import com.calvary.finance.user.response.RolesResponse;
import com.calvary.finance.user.service.RoleService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoleController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoleService roleService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void getRolesReturnsRoles() throws Exception {
        when(roleService.getRoles()).thenReturn(rolesResponse("Roles retrieved"));

        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Roles retrieved"))
                .andExpect(jsonPath("$.roles[0].name").value("admin"));
    }

    @Test
    void createRoleValidatesRequestBody() throws Exception {
        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("name"));
    }

    @Test
    void createRolePassesBodyToService() throws Exception {
        when(roleService.createRole(any(CreateNewRoleRequest.class))).thenReturn(rolesResponse("Role created"));

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "admin"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Role created"));
    }

    @Test
    void updateRolePassesPathVariableAndBodyToService() throws Exception {
        when(roleService.updateRole(eq(7L), any(UpdateRoleRequest.class))).thenReturn(rolesResponse("Role updated"));

        mockMvc.perform(put("/roles/{roleId}", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "finance"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Role updated"));

        verify(roleService).updateRole(eq(7L), any(UpdateRoleRequest.class));
    }

    @Test
    void deleteRolePassesPathVariableToService() throws Exception {
        when(roleService.deleteRole(7L)).thenReturn(rolesResponse("Role deleted"));

        mockMvc.perform(delete("/roles/{roleId}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Role deleted"));

        verify(roleService).deleteRole(7L);
    }

    @Test
    void changeUserRoleReturnsOkWhenServiceSucceeds() throws Exception {
        when(roleService.changeUserRole(7L, 42L)).thenReturn(true);

        mockMvc.perform(post("/roles/{roleId}/users/{userId}", 7L, 42L))
                .andExpect(status().isOk())
                .andExpect(content().string("Role changed successfully"));
    }

    @Test
    void changeUserRoleReturnsBadRequestWhenServiceFails() throws Exception {
        when(roleService.changeUserRole(7L, 42L)).thenReturn(false);

        mockMvc.perform(post("/roles/{roleId}/users/{userId}", 7L, 42L))
                .andExpect(status().isBadRequest());
    }

    private RolesResponse rolesResponse(String message) {
        return new RolesResponse()
                .setMessage(message)
                .setRoles(List.of(role()));
    }

    private Role role() {
        Role role = new Role();
        role.setId(7L);
        role.setName("admin");
        return role;
    }
}
