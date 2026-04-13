package com.calvary.finance.user.service;

import com.calvary.finance.user.entity.Role;
import com.calvary.finance.user.entity.User;
import com.calvary.finance.user.repository.RoleRepository;
import com.calvary.finance.user.repository.UserRepository;
import com.calvary.finance.user.request.CreateNewRoleRequest;
import com.calvary.finance.user.request.UpdateRoleRequest;
import com.calvary.finance.user.response.RolesResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public RoleService(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    public RolesResponse getRoles() {
        return new RolesResponse()
                .setMessage("Roles retrieved")
                .setRoles(roleRepository.findAll());
    }

    public RolesResponse getRole(Long roleId) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new EntityNotFoundException("Role not found"));

        return new RolesResponse()
                .setMessage("Role retrieved")
                .setRoles(List.of(role));
    }

    @Transactional
    public RolesResponse createRole(CreateNewRoleRequest request) {
        if (roleRepository.existsByNameIgnoreCase(request.getName())) {
            throw new RuntimeException("Role already exists");
        }

        Role role = new Role();
        role.setName(request.getName().trim().toLowerCase());
        Role savedRole = roleRepository.save(role);

        return new RolesResponse()
                .setMessage("Role created")
                .setRoles(List.of(savedRole));
    }

    @Transactional
    public RolesResponse updateRole(Long roleId, UpdateRoleRequest request) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new EntityNotFoundException("Role not found"));

        if (!role.getName().equalsIgnoreCase(request.getName()) && roleRepository.existsByNameIgnoreCase(request.getName())) {
            throw new RuntimeException("Role already exists");
        }

        role.setName(request.getName().trim().toLowerCase());
        Role savedRole = roleRepository.save(role);

        return new RolesResponse()
                .setMessage("Role updated")
                .setRoles(List.of(savedRole));
    }

    @Transactional
    public RolesResponse deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new EntityNotFoundException("Role not found"));
        if (userRepository.existsByRoleId(roleId)) {
            throw new RuntimeException("Role is assigned to one or more users");
        }

        roleRepository.delete(role);

        return new RolesResponse()
                .setMessage("Role deleted")
                .setRoles(List.of(role));
    }

    public Boolean changeUserRole(Long roleId, Long userId) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new EntityNotFoundException("Role not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setRoleId(roleId);
        user.setRoleCode(role.getName().trim().toUpperCase());
        userRepository.save(user);

        return user.getRoleId().equals(roleId) && user.getRoleCode().equalsIgnoreCase(role.getName());
    }
}
