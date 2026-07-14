package com.springboot.authentication.repository;

import com.springboot.authentication.dto.SendOtpRequest;
import com.springboot.authentication.entity.Otp;
import com.springboot.authentication.entity.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;


public interface OtpTokenRepository extends JpaRepository<Otp,Long> {

    @Modifying
    @Query("""
        UPDATE Otp o
        SET o.isUsed = true
        WHERE o.user.id = :userId
        AND o.isUsed = false
    """)
    void markAllAsUsed(Long userId, SendOtpRequest.OtpPurpose purpose);

    Optional<Otp> findTopByUserAndPurposeAndIsUsedFalseOrderByCreatedAtDesc(User user, SendOtpRequest.@NotNull(message = "Purpose is required") OtpPurpose purpose);

}
