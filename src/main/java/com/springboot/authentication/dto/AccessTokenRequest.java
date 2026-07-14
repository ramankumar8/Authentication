package com.springboot.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AccessTokenRequest {

    @NotBlank(message = "Refresh Token cannot be blank")
    private String refreshToken;

}
