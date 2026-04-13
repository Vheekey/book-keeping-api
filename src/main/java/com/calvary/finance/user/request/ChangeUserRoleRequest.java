package com.calvary.finance.user.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChangeUserRoleRequest {
    @NotBlank(message = "Role is required")
    private String role;
}
