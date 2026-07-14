package com.springboot.authentication.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.UUID;

@Data
public class VerifyOtpRequest {

    @NotNull(message = "User Id cannot be Null")
    private UUID userId;

    @Pattern(
            regexp = "[0-9]{6}$",
            message = "Invalid OTP"
    )
    private String otp;

    @NotNull(message = "Purpose is required")
    private SendOtpRequest.OtpPurpose purpose;

}
