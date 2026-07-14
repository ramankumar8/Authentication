package com.springboot.authentication.dto;

import lombok.Data;

@Data
public class VerifyOtpResponse {

    private String accessToken;
    private String refreshToken;

}
