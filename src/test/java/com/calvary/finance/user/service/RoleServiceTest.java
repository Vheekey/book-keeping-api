package com.calvary.finance.user.service;

import com.calvary.finance.user.entity.Role;
import com.calvary.finance.user.entity.User;
import com.calvary.finance.user.repository.RoleRepository;
import com.calvary.finance.user.repository.UserRepository;
import com.calvary.finance.user.request.CreateNewRoleRequest;
import com.calvary.finance.user.request.UpdateRoleRequest;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoleService roleService;

    @Test
    void createRoleNormalizesNameAndRejectsDuplicates() {
        CreateNewRoleRequest request = new CreateNewRoleRequest();
        request.setName(" Admin ");

        when(roleRepository.existsByNameIgnoreCase(" Admin ")).thenReturn(false);
        when(roleRepository.save(org.mockito.ArgumentMatchers.any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Role savedRole = roleService.createRole(request).getRoles().get(0);

        assertThat(savedRole.getName()).isEqualTo("admin");
    }

    @Test
    void createRoleRejectsDuplicateRoleName() {
        CreateNewRoleRequest request = new CreateNewRoleRequest();
        request.setName("admin");
        when(roleRepository.existsByNameIgnoreCase("admin")).thenReturn(true);

        assertThatThrownBy(() -> roleService.createRole(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Role already exists");
    }

    @Test
    void deleteRoleRejectsAssignedRole() {
        Role role = role(7L, "admin");
        when(roleRepository.findById(7L)).thenReturn(Optional.of(role));
        when(userRepository.existsByRoleId(7L)).thenReturn(true);

        assertThatThrownBy(() -> roleService.deleteRole(7L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Role is assigned to one or more users");

        verify(roleRepository, never()).delete(role);
    }

    @Test
    void updateRoleThrowsWhenRoleDoesNotExist() {
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setName("admin");
        when(roleRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.updateRole(404L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Role not found");
    }

    @Test
    void changeUserRoleUpdatesUserRoleAndCode() {
        Role role = role(7L, "admin");
        User user = new User();
        user.setId(42L);
        user.setRoleId(1L);
        user.setRoleCode("USER");

        when(roleRepository.findById(7L)).thenReturn(Optional.of(role));
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));

        Boolean changed = roleService.changeUserRole(7L, 42L);

        assertThat(changed).isTrue();
        assertThat(user.getRoleId()).isEqualTo(7L);
        assertThat(user.getRoleCode()).isEqualTo("ADMIN");
        verify(userRepository).save(user);
    }

    private Role role(Long id, String name) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        return role;
    }
}
