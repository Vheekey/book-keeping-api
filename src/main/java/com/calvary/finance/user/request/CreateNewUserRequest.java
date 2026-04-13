package com.calvary.finance.user.request;

import com.calvary.finance.user.request.validation.UniqueEmail;
import com.calvary.finance.user.request.validation.UniqueUsername;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateNewUserRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email
    @UniqueEmail
    private String email;

    @NotBlank(message = "Password field cannot be empty")
    private String password;

    @Nullable
    @Size(min = 5, max = 100)
    @UniqueUsername
    private String username;
}
