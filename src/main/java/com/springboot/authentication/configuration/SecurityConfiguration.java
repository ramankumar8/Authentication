package com.springboot.authentication.configuration;

import com.springboot.authentication.component.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(csrf -> csrf.disable()) // Disable CSRF (not needed for token-based auth like JWT)
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Enable CORS and use your custom configuration
                // Make the app stateless (no HTTP session; every request must carry JWT)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Allow everyone to access /public/** endpoints
                        .requestMatchers("/public/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/auth/send-otp",
                                "/auth/verify-otp",
                                "/auth/access-token").permitAll()
                        .anyRequest().authenticated() // All other endpoints require authentication
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(AbstractHttpConfigurer::disable) // Disable default Spring Security login form (we'll use JWT instead)
                .httpBasic(AbstractHttpConfigurer::disable); // Disable HTTP Basic Auth (username/password popup in browser)
        // Build and return the filter chain
        return httpSecurity.build();
    }

    // Need to add this for our frontend able to use this else browser will block the request with CORS(Cross Origin Resource Sharing).
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("https://localhost:3000")); // TODO-PROD: list of allowed origins
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*")); // TODO-PROD: Need to change this on production. (List.of("Authorization", "Content-Type")
        configuration.setAllowCredentials(true); // Allows cookies, Authorization headers, or other credentials to be sent in cross-origin requests.
        configuration.setMaxAge(3600L);// Tells the browser how long (in seconds) to cache the CORS preflight response (OPTIONS request).

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("User not found");
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
