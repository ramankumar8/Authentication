package com.springboot.authentication.utility;

import com.springboot.authentication.entity.RefreshToken;
import com.springboot.authentication.entity.User;
import com.springboot.authentication.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.xml.bind.DatatypeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private String secretKeyString = "DASYWgfhMLL0np41rKFAGminD1zb5DlwDzE1WwnP8es=";

    private Key secretKey;

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public String generateRefreshToken(User user, String ipAddress, String userAgent) {

        try {

            String uniqueTokenId = UUID.randomUUID().toString();

            boolean isMobile = isMobileDevice(userAgent);

            LocalDateTime expiryTime;

            if (isMobile) {
                expiryTime = LocalDateTime.now()
                        .plusDays(Constants.mobileRefreshTokenExpiryDays);
            } else {
                expiryTime = LocalDateTime.now()
                        .plusMinutes(Constants.refreshTokenExpiryTimeInMinutes);
            }


            Date expiryDate = Date.from(
                    expiryTime.atZone(ZoneId.systemDefault()).toInstant()
            );


            String refreshTokenString = Jwts.builder()
                    .setHeaderParam("typ", "JWT")
                    .setId(uniqueTokenId)
                    .claim("id", user.getPublicId())
                    .claim("role", user.getRole())
                    .claim("type", Constants.refreshTokenType)
                    .claim("userAgent", userAgent)
                    .claim("ipAddress", ipAddress)
                    .setIssuedAt(new Date())
                    .setExpiration(expiryDate)
                    .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                    .compact();


            RefreshToken refreshToken = new RefreshToken();

            refreshToken.setStatus(Constants.Status.active);
            refreshToken.setUser(user);
            refreshToken.setToken(refreshTokenString);
            refreshToken.setExpiryTime(expiryTime);
            refreshToken.setIpAddress(ipAddress);
            refreshToken.setUserAgent(userAgent);
            refreshToken.setDeviceName(getDeviceName(userAgent));

            refreshTokenRepository.save(refreshToken);

            return refreshTokenString;

        } catch (Exception e) {
            throw new RuntimeException("Error generating JWT token", e);
        }
    }

    private boolean isMobileDevice(String userAgent) {
        String devicePattern = "android|webos|iphone|ipad|ipod|blackberry|iemobile|opera mini";
        return userAgent != null && userAgent.toLowerCase().matches(".*(" + devicePattern + ").*");
    }

    public String getDeviceName(String userAgent) {

        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown Device";
        }
        if (userAgent.contains("iPhone")) {
            return "iPhone";
        }
        if (userAgent.contains("iPad")) {
            return "iPad";
        }
        if (userAgent.contains("Android")) {
            if (userAgent.contains("Mobile")) {
                return "Android Mobile";
            }
            return "Android Tablet";
        }
        if (userAgent.contains("Windows")) {
            return "Windows PC";
        }
        if (userAgent.contains("Macintosh")) {
            return "MacBook/Mac";
        }
        if (userAgent.contains("Linux")) {
            return "Linux Machine";
        }
        return "Unknown Device";
    }

    private Key getSignInKey() {

        try {
            byte[] secretKeyBytes = DatatypeConverter.parseBase64Binary(secretKeyString);
            this.secretKey = Keys.hmacShaKeyFor(secretKeyBytes);

            return this.secretKey;
        } catch (Exception exception) {
//            exceptionHandling.handleException(exception);
            throw new RuntimeException("Error generating JWT token", exception);
        }

    }

    public String extractUserAgent(String token) {
        try {
            if (token == null || token.isEmpty()) {
                throw new IllegalArgumentException("Token is required");
            }

            return Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("userAgent", String.class);

        }
/*        catch (SignatureException signatureException) {
            exceptionHandling.handleException(signatureException);
            throw new RuntimeException("Invalid JWT signature.");
        }*/ catch (Exception exception) {
//            exceptionHandling.handleException(exception);
            throw new RuntimeException("Error extracting userAgent from JWT token", exception);
        }
    }

    public Date getExpiryTime(String token) {
        try {
            if (token == null || token.isEmpty()) {
                throw new IllegalArgumentException("Token is required");
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getExpiration();

        } catch (ExpiredJwtException e) {
//            logoutUser(token);
            throw new ExpiredJwtException(null, null, "Token is expired");
        } catch (Exception e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    private boolean isTokenExpired(String token, String userAgent) {
        try {
            if (token == null || token.trim().isEmpty()) {
                throw new IllegalArgumentException("Token is required");
            }

            boolean isMobile = isMobileDevice(userAgent);

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();

            if (isMobile && expiration == null) {
                return false; // Mobile token doesn't have expiration
            }

            return expiration != null && expiration.before(new Date());

        } catch (ExpiredJwtException e) {
//            logoutUser(token);
            throw new ExpiredJwtException(e.getHeader(), e.getClaims(), "Token is expired and cannot be used.");
        } catch (MalformedJwtException e) {
//            exceptionHandling.handleException(e);
            throw new RuntimeException("Invalid JWT token", e);
        } catch (Exception e) {
//            exceptionHandling.handleException(e);
            throw new RuntimeException("Error checking token expiration", e);
        }
    }

    public String generateAccessToken(UUID publicId, String role, String ipAddress, String userAgent) {
        try {
            String uniqueTokenId = UUID.randomUUID().toString();

            boolean isMobile = isMobileDevice(userAgent);

            JwtBuilder jwtBuilder = Jwts.builder()
                    .setHeaderParam("typ", "JWT")
                    .setId(uniqueTokenId)
                    .claim("id", publicId)
                    .claim("role", role)
                    .claim("type", Constants.acessTokenType)
                    .claim("userAgent", userAgent)
                    .claim("ipAddress", ipAddress)
                    .setIssuedAt(new Date())
                    .signWith(getSignInKey(), SignatureAlgorithm.HS256);

            if (!isMobile) {
                jwtBuilder.setExpiration(new Date(System.currentTimeMillis() + (1000 * 60 * Constants.accessTokenExpiryTimeInMinutes)));
            }

            return jwtBuilder.compact();

        } catch (Exception e) {
//            exceptionHandling.handleException(e);
            throw new RuntimeException("Error generating JWT token", e);
        }
    }

    public UUID extractId(String token) {

        try {
            if (token == null || token.isEmpty()) {
                throw new JwtException("Token is required");
            }

            String userAgent = extractUserAgent(token);

            if (isTokenExpired(token, userAgent)) {
                throw new ExpiredJwtException(null, null, "Token is expired");
            }

            String id = Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("id", String.class);

            return UUID.fromString(id);

        } catch (ExpiredJwtException e) {
//            logoutUser(token);
            throw e;
        } catch (Exception e) {
//            exceptionHandling.handleException(e);
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public String extractTokenType(String token) {
        try {
            if (token == null || token.isEmpty()) {
                throw new IllegalArgumentException("Token is required");
            }

            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("type", String.class);

        } catch (Exception exception) {
//            exceptionHandling.handleException(exception);
            throw new RuntimeException("Error in JWT token", exception);
        }
    }

    public String extractRole(String token) {
        try {
            if (token == null || token.isEmpty()) {
                throw new IllegalArgumentException("Token is required");
            }

            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("role", String.class);

        } catch (Exception exception) {
//            exceptionHandling.handleException(exception);
            throw new RuntimeException("Error in JWT token", exception);
        }
    }

    @Transactional
    public Boolean validateToken(String token, String ipAddress, String userAgent) {

        try {

            if (token == null || token.isEmpty()) {
                throw new IllegalArgumentException("Token is required");
            }
            if (isTokenExpired(token, userAgent)) {
                throw new IllegalArgumentException("Token is expired");
            }

            UUID id = extractId(token);
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Blacklist Token
            /*if (tokenBlacklist.isTokenBlacklisted(tokenId)) {
                return false;
            }*/

            String storedIpAddress = claims.get("ipAddress", String.class);
            return ipAddress.trim().equals(storedIpAddress != null ? storedIpAddress.trim() : "");

        } catch (ExpiredJwtException expiredJwtException) {
//            logoutUser(token);
            return false;
        } catch (MalformedJwtException | IllegalArgumentException e) {
//            exceptionHandling.handleException(e);
            return false;
        } catch (Exception exception) {
//            exceptionHandling.handleException(exception);
            return false;
        }
    }

}
