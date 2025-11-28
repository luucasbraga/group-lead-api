package com.grouplead.service.core;

import com.grouplead.config.security.JwtService;
import com.grouplead.domain.entity.User;
import com.grouplead.domain.enums.UserRole;
import com.grouplead.dto.request.LoginRequest;
import com.grouplead.dto.request.RegisterRequest;
import com.grouplead.dto.response.AuthResponse;
import com.grouplead.exception.AuthenticationException;
import com.grouplead.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.username());

        if (userRepository.existsByUsername(request.username())) {
            throw new AuthenticationException("Username already exists: " + request.username());
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new AuthenticationException("Email already exists: " + request.email());
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role(UserRole.USER)
                .active(true)
                .build();

        user = userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.of(
                token,
                jwtService.getExpirationTime(),
                new AuthResponse.UserInfo(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getRole().name()
                )
        );
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Authenticating user: {}", request.username());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (Exception e) {
            throw new AuthenticationException("Invalid username or password");
        }

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new AuthenticationException("User not found"));

        if (!user.getActive()) {
            throw new AuthenticationException("User account is disabled");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.of(
                token,
                jwtService.getExpirationTime(),
                new AuthResponse.UserInfo(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getRole().name()
                )
        );
    }
}
