package com.calvary.finance.user.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateNewRoleRequest {
    @NotBlank(message = "Role Name is required")
    private String name;
}
