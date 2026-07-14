package com.springboot.authentication.component;

import com.springboot.authentication.entity.User;
import com.springboot.authentication.repository.UserRepository;
import com.springboot.authentication.utility.Constants;
import com.springboot.authentication.utility.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = BEARER_PREFIX.length();
    private String apiKey = "IaJGL98yHnKjnlhKshiWiy1IhZ+uFsKnktaqFX3Dvfg=";

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {

            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                filterChain.doFilter(request, response);
                return;
            }

            String requestURI = request.getRequestURI();
            log.debug("Incoming request: {}", requestURI);

            // JWT validation logic will go here later
            log.info("Incoming Request is: ");

            /*if (isUnsecuredUri(requestURI)) {
                filterChain.doFilter(request, response);
                return;
            }*/
            if (isApiKeyRequiredUri(request) && validateApiKey(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            authenticateUser(request, response);
//            if (!responseHandled) {
//                filterChain.doFilter(request, response);
//            }

        } catch (Exception e) {
            log.error("JWT filter error", e);
        }

        // Always continue the chain
        filterChain.doFilter(request, response);
    }

    private boolean isApiKeyRequiredUri(HttpServletRequest request) {

        String requestURI = request.getRequestURI();
        String path = requestURI.split("\\?")[0].trim();

        List<Pattern> bypassPatterns = Arrays.asList(
                // add your endpoints which needs x-api key
                Pattern.compile("^/api/v1/category-custom/get-products-by-category-id/\\d+$"),
                Pattern.compile("^/api/v1/category-custom/get-all-categories$")
        );

        return bypassPatterns.stream().anyMatch(pattern -> pattern.matcher(path).matches());
    }

    private boolean validateApiKey(HttpServletRequest request) {
        String requestApiKey = request.getHeader("x-api-key");
        return apiKey.equals(requestApiKey);
    }

    private void authenticateUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new JwtException("JWT token cannot be empty");
        }

        String jwt = authorizationHeader.substring(BEARER_PREFIX_LENGTH);
        UUID id = jwtUtil.extractId(jwt);

        // Blacklisted Token.
        /*if (tokenBlacklist.isTokenBlacklisted(jwt)) {
            respondWithUnauthorized(response, "Token has been blacklisted");
            return true;
        }*/

        String ipAddress = request.getRemoteAddr();
        String User_Agent = request.getHeader("User-Agent");

        try {
            if (!jwtUtil.validateToken(jwt, ipAddress, User_Agent)) {
                throw new JwtException("Invalid JWT token");
            }
        } catch (Exception exception) {
//            jwtUtil.logoutUser(jwt);
//            respondWithUnauthorized(response, "Token is expired");
        }

        String tokenType = jwtUtil.extractTokenType(jwt);
        if(!tokenType.equalsIgnoreCase(Constants.acessTokenType.toString())) {
            throw new JwtException("Invalid Token Type.");
        }

        if (id != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.extractRole(jwt).equalsIgnoreCase(Constants.ROLE_USER)) {

                User user = userRepository.findByPublicIdAndStatusAndIsLocked(id, Constants.Status.active, false).orElseThrow( () -> new IllegalArgumentException(Constants.userNotFound));

                if (user != null && jwtUtil.validateToken(jwt, ipAddress, User_Agent)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            user.getId(), null, new ArrayList<>());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
//                    jwtUtil.logoutUser(jwt);
//                    respondWithUnauthorized(response, "Invalid data provided for this student");
                }
            }
        }
    }

}
