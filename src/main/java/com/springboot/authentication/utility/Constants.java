package com.springboot.authentication.utility;

import java.time.Duration;
import java.time.temporal.TemporalAmount;

public class Constants {

    public static final String ROLE_USER = "User";

    // Global
    public static Status activeStatus = Status.active;
    public static Status inactiveStatus = Status.inactive;
    public static Long refreshTokenExpiryTimeInMinutes = 15L * 60L;
    public static Long accessTokenExpiryTimeInMinutes = 15L;
    public static Long mobileRefreshTokenExpiryDays = 15L;

    // OTP
    public static final TemporalAmount LOG_IN_PURPOSE_EXPIRATION_TIME = Duration.ofMinutes(8);
    public static final Channel emailChannel = Channel.email;
    public static final Channel phoneChannel = Channel.phone;
    public static final TokenType refreshTokenType = TokenType.refresh;
    public static final TokenType acessTokenType = TokenType.access;

    // Exception Messages.
    public static final String unauthorized = "Unauthorized";
    public static final String unknown = "Something Went Wrong";
    public static final String validationError = "Validation Error";
    public static final String invalidChannelInOtpRequest = "Invalid Channel Found in the request.";
    public static final String userNotFound = "User Not Found";
    public static final String otpNotFound = "OTP Not Found";
    public static final String otpExpired = "OTP has expired";
    public static final String invalidOTP = "Invalid OTP";


    // ENUMS
    public enum Status {
        active,
        inactive
    }

    public enum Channel {
        email,
        phone
    }

    public enum TokenType {
        refresh,
        access
    }

    public enum Salutation {
        MR,
        MRS,
        MS,
        MISS,
        DR,
        PROF
    }

}
