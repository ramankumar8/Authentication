package com.springboot.authentication.repository;

import com.springboot.authentication.entity.User;
import com.springboot.authentication.utility.Constants;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    Optional<User> findByPublicIdAndStatusAndIsLocked(UUID publicId, Constants.Status status, Boolean isLocked);
}
