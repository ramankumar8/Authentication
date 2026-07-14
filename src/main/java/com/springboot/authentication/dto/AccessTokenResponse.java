package com.springboot.authentication.dto;

import lombok.Data;

@Data
public class AccessTokenResponse {

    private String refreshToken;
    private String accessToken;

}
