package com.springboot.authentication.dto;

import com.springboot.authentication.utility.Constants;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserDetailsDTO {

    private UUID publicId;
    public Constants.Salutation salutation;
    public String firstName;
    public String middleName;
    public String lastName;
    public String fullName;
    private String email;
    private String phone;
    private String role;
    private boolean isEmailVerified;
    private boolean isPhoneVerified;
    private boolean isLocked;
    private Boolean is2FaEnabled;
    private LocalDateTime createdAt;
    private String createdByName;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private String updatedByName;
    private Long updatedBy;

}
