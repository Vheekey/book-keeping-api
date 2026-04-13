package com.calvary.finance.user.controller;

import com.calvary.finance.user.request.CreateNewRoleRequest;
import com.calvary.finance.user.request.UpdateRoleRequest;
import com.calvary.finance.user.response.RolesResponse;
import com.calvary.finance.user.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/roles")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService){
        this.roleService = roleService;
    }

    @PreAuthorize("hasRole('SADMIN')")
    @GetMapping
    public ResponseEntity<RolesResponse> getRoles() {
        RolesResponse response = roleService.getRoles();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SADMIN')")
    @GetMapping("/{roleId}")
    public ResponseEntity<RolesResponse> getRole(@PathVariable Long roleId) {
        RolesResponse response = roleService.getRole(roleId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SADMIN')")
    @PostMapping
    public ResponseEntity<RolesResponse> createRole(@Valid @RequestBody CreateNewRoleRequest createNewRoleRequest) {
        RolesResponse response = roleService.createRole(createNewRoleRequest);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SADMIN')")
    @PutMapping("/{roleId}")
    public ResponseEntity<RolesResponse> updateRole(
            @PathVariable Long roleId,
            @Valid @RequestBody UpdateRoleRequest updateRoleRequest
    ) {
        RolesResponse response = roleService.updateRole(roleId, updateRoleRequest);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SADMIN')")
    @DeleteMapping("/{roleId}")
    public ResponseEntity<RolesResponse> deleteRole(@PathVariable Long roleId) {
        RolesResponse response = roleService.deleteRole(roleId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SADMIN')")
    @PostMapping("/{roleId}/users/{userId}")
    public ResponseEntity<String> changeUserRole(@PathVariable Long roleId, @PathVariable Long userId){
        Boolean hasUserRoleChanged = roleService.changeUserRole(roleId, userId);
        if(Boolean.FALSE.equals(hasUserRoleChanged)){
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok("Role changed successfully");
    }
}
