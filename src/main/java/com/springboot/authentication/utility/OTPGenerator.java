package com.springboot.authentication.utility;

import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;

@Slf4j
public class OTPGenerator {

    private static final SecureRandom random = new SecureRandom();

    public static String generateOtp() {
        int otp = 100000 + random.nextInt(900000); // ensures 6-digit (100000–999999) as 90,000 is exclusive and 0 is inclusive.
        return String.valueOf(otp);
    }

}
