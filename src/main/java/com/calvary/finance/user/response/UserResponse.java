package com.calvary.finance.user.response;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Long roleId;
    private String roleName;
    private String username;
    private Boolean isActive;
}
