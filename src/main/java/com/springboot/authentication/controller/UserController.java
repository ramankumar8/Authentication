package com.springboot.authentication.controller;

import com.springboot.authentication.common.ApiResponse;
import com.springboot.authentication.dto.UserDetailsDTO;
import com.springboot.authentication.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("details/{id}")
    public ResponseEntity<?> sendOtp(@PathVariable UUID id) {
        UserDetailsDTO userDetail = userService.getUserDetail(id);
        return ResponseEntity.ok(ApiResponse.success("User Details Fetched Successfully", userDetail));
    }

}
