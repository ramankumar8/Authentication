package com.springboot.authentication.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PasswordResetToken {

    @Id
    private UUID id;

//    @Column(unique = true, nullable = false, updatable = false)
//    private UUID publicId;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiryTime;

    private boolean used = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime createdAt;

    @PrePersist
    public void generatePublicId() {
//        if (this.publicId == null) {
//            this.publicId = UUID.randomUUID();
//        }
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }

}