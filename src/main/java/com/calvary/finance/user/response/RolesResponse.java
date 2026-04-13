package com.calvary.finance.user.response;

import com.calvary.finance.user.entity.Role;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class RolesResponse {
    private String message;
    private List<Role> roles;
}
