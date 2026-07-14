package com.springboot.authentication.common;

import com.springboot.authentication.utility.Constants;
import exception.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException authenticationException) {
        log.info(authenticationException.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(authenticationException.getMessage()));
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleValidationError(IllegalArgumentException illegalArgumentException) {
        log.info(illegalArgumentException.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(), Constants.validationError, illegalArgumentException.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        log.info(exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Something went wrong"));
    }

}
