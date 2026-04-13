package com.calvary.finance.user.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class AuthResponse {
    private String token;
    private UserResponse user;
}
