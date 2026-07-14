package com.springboot.authentication.controller;

import com.springboot.authentication.dto.AccessTokenRequest;
import com.springboot.authentication.common.ApiResponse;
import com.springboot.authentication.dto.SendOtpRequest;
import com.springboot.authentication.dto.VerifyOtpRequest;
import com.springboot.authentication.dto.VerifyOtpResponse;
import com.springboot.authentication.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("send-otp")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody SendOtpRequest sendOtpRequest) {
        authenticationService.sendOtp(sendOtpRequest);
        return ResponseEntity.ok(ApiResponse.success("Otp Send Successfully", null));
    }

    @PostMapping("verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest verifyOtpRequest, HttpServletRequest httpServletRequest) {
        VerifyOtpResponse verifyOtpResponse = authenticationService.verifyOtp(verifyOtpRequest, httpServletRequest);
        return ResponseEntity.ok(ApiResponse.success("Otp Verified Successfully", verifyOtpResponse));
    }

    @PostMapping("access-token")
    public ResponseEntity<?> generateAccessToken(@Valid @RequestBody AccessTokenRequest accessTokenRequest, HttpServletRequest httpServletRequest) {
        String accessToken = authenticationService.generateAccessToken(accessTokenRequest, httpServletRequest);
        return ResponseEntity.ok(ApiResponse.success("Access Token Generated Successfully", accessToken));
    }

    /*@PostMapping("login")
    public ResponseEntity<?> login() {

    }*/


}
