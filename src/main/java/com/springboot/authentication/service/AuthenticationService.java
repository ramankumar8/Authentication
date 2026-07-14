package com.springboot.authentication.service;

import com.springboot.authentication.dto.AccessTokenRequest;
import com.springboot.authentication.dto.SendOtpRequest;
import com.springboot.authentication.dto.VerifyOtpRequest;
import com.springboot.authentication.dto.VerifyOtpResponse;
import com.springboot.authentication.entity.Otp;
import com.springboot.authentication.entity.User;
import com.springboot.authentication.repository.OtpTokenRepository;
import com.springboot.authentication.repository.UserRepository;
import com.springboot.authentication.utility.Constants;
import com.springboot.authentication.utility.JwtUtil;
import com.springboot.authentication.utility.OTPGenerator;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final OtpTokenRepository otpRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public void sendOtp(SendOtpRequest request) {

        // create or find user.
        User user = findOrCreateUser(request);
        // invalidate old OTPs if exists for old user.
        invalidateOldOTPs(user, request.getPurpose());

        // OTP generation
        String rawOtp = OTPGenerator.generateOtp();
        String hashedOtp = passwordEncoder.encode(rawOtp);

        saveOtp(user, hashedOtp, request.getEmail(), request.getPhone(), request.getPurpose());
        sendOtpToUser(user, request, rawOtp);
    }

    @Async
    private void sendOtpToUser(User user, SendOtpRequest request, String rawOtp) {

        if (request.getEmail() != null) {
            // send otp via mail.
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(request.getEmail());
            message.setSubject("Your OTP Code");
            message.setFrom("ramandashalternate@gmail.com");
            message.setText("Your OTP is: " + rawOtp + "\nIt will expire in 5 minutes.");

            mailSender.send(message);

            log.info("OTP sent to email {}", request.getEmail());
        } else if (request.getPhone() != null) {
            // Send OTP via Twilio SMS
            Twilio.init(accountSid, authToken);

            Message message = Message.creator(
                    new PhoneNumber("+91" + request.getPhone()),
                    new PhoneNumber(twilioPhoneNumber),
                    "Your OTP is: " + rawOtp +
                            ". It will expire in 5 minutes."
            ).create();

            log.info("OTP sent to phone {}", request.getPhone());

        } else {
            throw new UnsupportedOperationException("Request Does not contains either email or phone.");
        }

    }

    @Transactional
    private void saveOtp(User user, String hashedOtp, String email, String phone, SendOtpRequest.@NotEmpty(message = "Purpose is required") OtpPurpose purpose) {

        Otp otp = new Otp();
        otp.setUsed(false);
        otp.setPurpose(purpose);
        otp.setOtp(hashedOtp);
        otp.setExpiryTime(LocalDateTime.now().plus(Constants.LOG_IN_PURPOSE_EXPIRATION_TIME));
        otp.setUser(user);
        otp.setStatus(Constants.activeStatus);

        if(email != null && !email.trim().isEmpty()) {
            otp.setChannel(Constants.emailChannel);
        } else if(phone != null && !phone.trim().isEmpty()) {
            otp.setChannel(Constants.phoneChannel);
        } else {
            throw new IllegalArgumentException(Constants.invalidChannelInOtpRequest);
        }

        otpRepository.save(otp);
    }

    @Transactional
    private User findOrCreateUser(SendOtpRequest request) {

        Optional<User> optionalUser;

        if (request.getEmail() != null) {
            optionalUser = userRepository.findByEmail(request.getEmail());
        } else if (request.getPhone() != null) {
            optionalUser = userRepository.findByPhone(request.getPhone());
        } else {
            throw new UnsupportedOperationException("Request Does not contains either email or phone.");
        }

        return optionalUser.orElseGet(() -> {
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setStatus(Constants.activeStatus);
            return userRepository.save(user);
        });
    }

    @Transactional
    private void invalidateOldOTPs(User user, SendOtpRequest.OtpPurpose purpose) {
        otpRepository.markAllAsUsed(user.getId(), purpose);
    }

    @Transactional
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest verifyOtpRequest, HttpServletRequest httpServletRequest) {

        // 1. verify the user based on public userId
        User user = userRepository.findByPublicIdAndStatusAndIsLocked(verifyOtpRequest.getUserId(), Constants.activeStatus, false)
                .orElseThrow(() -> new IllegalArgumentException(Constants.userNotFound));

        // 2. verify the Otp (Based on Purpose)
        Otp otp = otpRepository
                .findTopByUserAndPurposeAndIsUsedFalseOrderByCreatedAtDesc(
                        user,
                        verifyOtpRequest.getPurpose())
                .orElseThrow(() -> new IllegalArgumentException(Constants.otpNotFound));

        if (otp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(Constants.otpExpired);
        }

        if (!passwordEncoder.matches(verifyOtpRequest.getOtp(), otp.getOtp())) {
            otp.setAttemptCount(otp.getAttemptCount() + 1);
            throw new IllegalArgumentException(Constants.invalidOTP);
        }

        otp.setUsed(true);
        otpRepository.save(otp);

//        String tokenKey = "authToken_" + faculty.getPersonalEmail();
        String ipAddress = httpServletRequest.getRemoteAddr();
        String userAgent = httpServletRequest.getHeader("User-Agent");

        // 3. Generate Refresh Token (Long lived 15 hours). and store it in db.
        String refreshTokenString = jwtUtil.generateRefreshToken(user, ipAddress, userAgent);

        // 4. Generate Access Token with Refresh Token(Short Lived 15 minutes).
        String accessToken = jwtUtil.generateAccessToken(user.getPublicId(), user.getRole(), ipAddress, userAgent);
        // 5. Response.

        VerifyOtpResponse response = new VerifyOtpResponse();
        response.setRefreshToken(refreshTokenString);
        response.setAccessToken(accessToken);

        return response;

    }

    public String generateAccessToken(@Valid AccessTokenRequest accessTokenRequest, HttpServletRequest httpServletRequest) {

        // 1. Verify User First from the refresh Token.
        String refreshToken = accessTokenRequest.getRefreshToken();
        UUID userPublicId = jwtUtil.extractId(refreshToken);

        User user = userRepository.findByPublicIdAndStatusAndIsLocked(userPublicId, Constants.activeStatus, false)
                .orElseThrow(() -> new IllegalArgumentException(Constants.userNotFound));

        // 2. Generate Access Token
        String ipAddress = httpServletRequest.getRemoteAddr();
        String userAgent = httpServletRequest.getHeader("User-Agent");

        return jwtUtil.generateAccessToken(userPublicId, user.getRole(), ipAddress, userAgent);

    }
}
