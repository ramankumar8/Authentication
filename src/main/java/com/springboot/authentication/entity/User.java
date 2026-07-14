package com.springboot.authentication.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.springboot.authentication.utility.Constants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", unique = true, nullable = false, updatable = false)
    private UUID publicId;

    @Enumerated(EnumType.STRING)
    @Column(name = "salutation")
    private Constants.Salutation salutation;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "full_name")
    private String fullName;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phone;

    @JsonIgnore
    private String password;

    @Column(nullable = false)
    private String role = "USER";

    @Column(name = "is_email_verified", nullable = false)
    private boolean isEmailVerified = false;

    @Column(name = "is_phone_verified", nullable = false)
    private boolean isPhoneVerified = false;

    @Column(name = "is_locked", nullable = false)
    private boolean isLocked = false;

    @Column(name = "is_2fa_enabled", nullable = false)
    private Boolean is2FaEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Constants.Status status;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by")
    private Long createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @PrePersist
    public void generatePublicId() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
    }

}