package com.springboot.authentication.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SendOtpRequest {

    @Email(message = "Invalid email format")
    private String email;

    @Pattern(
            regexp = "^[6-9][0-9]{9}$",
            message = "Invalid phone number"
    )
    private String phone;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Purpose is required")
    private OtpPurpose purpose;

    public enum OtpPurpose {
        LOG_IN,
        PASSWORD_RESET,
        EMAIL_VERIFICATION,
        PHONE_VERIFICATION,
        TWO_FACTOR_AUTH
    }
}
