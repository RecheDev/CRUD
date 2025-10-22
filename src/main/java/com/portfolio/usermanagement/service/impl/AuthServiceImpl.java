package com.portfolio.usermanagement.service.impl;

import com.portfolio.usermanagement.dto.request.LoginRequest;
import com.portfolio.usermanagement.dto.request.LogoutRequest;
import com.portfolio.usermanagement.dto.request.RefreshTokenRequest;
import com.portfolio.usermanagement.dto.request.RegisterRequest;
import com.portfolio.usermanagement.dto.response.AuthResponse;
import com.portfolio.usermanagement.dto.response.UserResponse;
import com.portfolio.usermanagement.entity.RefreshToken;
import com.portfolio.usermanagement.entity.Role;
import com.portfolio.usermanagement.entity.User;
import com.portfolio.usermanagement.exception.BadRequestException;
import com.portfolio.usermanagement.exception.ConflictException;
import com.portfolio.usermanagement.exception.ErrorCode;
import com.portfolio.usermanagement.repository.RoleRepository;
import com.portfolio.usermanagement.repository.UserRepository;
import com.portfolio.usermanagement.security.AccountLockoutService;
import com.portfolio.usermanagement.security.RefreshTokenService;
import com.portfolio.usermanagement.security.jwt.JwtUtils;
import com.portfolio.usermanagement.security.jwt.TokenBlacklistService;
import com.portfolio.usermanagement.monitoring.MetricsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthServiceImpl {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AccountLockoutService accountLockoutService;

    @Autowired
    private MetricsService metricsService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private UserDetailsService userDetailsService;

    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username is already taken", ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already in use", ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(true)
                .accountNonLocked(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .build();

        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        // Record user registration metric
        metricsService.recordUserRegistration();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Create refresh token
        String ipAddress = getClientIP(httpRequest);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser, ipAddress);

        return AuthResponse.builder()
                .token(jwt)
                .refreshToken(refreshToken.getToken())
                .user(convertToResponse(savedUser))
                .build();
    }

    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String username = request.getUsername();

        // Check if account is locked due to previous failed attempts
        if (accountLockoutService.isLocked(username)) {
            java.time.Duration remainingTime = accountLockoutService.getRemainingLockoutTime(username);
            long minutesRemaining = remainingTime != null ? remainingTime.toMinutes() : 0;
            throw new LockedException(
                String.format("Account is locked due to multiple failed login attempts. Try again in %d minutes.",
                    minutesRemaining)
            );
        }

        try {
            // Attempt authentication with provided credentials
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            // Login succeeded - clear any failed attempt tracking
            accountLockoutService.loginSucceeded(username);

            // Record successful login metric
            metricsService.recordLoginSuccess(username);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BadRequestException("User not found"));

            // Create refresh token
            String ipAddress = getClientIP(httpRequest);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, ipAddress);

            return AuthResponse.builder()
                    .token(jwt)
                    .refreshToken(refreshToken.getToken())
                    .user(convertToResponse(user))
                    .build();
        } catch (BadCredentialsException ex) {
            // Record failed login attempt for brute force protection
            accountLockoutService.loginFailed(username);

            // Record failed login metric
            metricsService.recordLoginFailure(username, "bad_credentials");

            // Check if this failure triggered an account lockout
            if (accountLockoutService.isLocked(username)) {
                metricsService.recordSecurityEvent("account_locked");
                throw new LockedException("Account locked due to multiple failed login attempts. Try again in 30 minutes.");
            }

            // Re-throw the original exception for normal error handling
            throw ex;
        }
    }

    /**
     * Logout user by blacklisting access token and revoking refresh token.
     *
     * @param logoutRequest contains the refresh token to revoke
     * @param accessToken the JWT access token (from Authorization header)
     */
    public void logout(LogoutRequest logoutRequest, String accessToken) {
        // Revoke the refresh token
        refreshTokenService.revokeRefreshToken(logoutRequest.getRefreshToken());

        // Blacklist the access token (JWT)
        if (accessToken != null) {
            String jti = jwtUtils.getJtiFromToken(accessToken);
            long expirationMs = jwtUtils.getExpirationMs(accessToken);
            Date expiry = new Date(System.currentTimeMillis() + expirationMs);
            tokenBlacklistService.blacklistToken(jti, expiry);
        }

        metricsService.recordSecurityEvent("user_logout");
    }

    /**
     * Refresh the access token using a valid refresh token.
     * Implements token rotation for security: old refresh token is revoked,
     * new access and refresh tokens are issued.
     *
     * @param request contains the current refresh token
     * @param httpRequest for IP address tracking
     * @return new AuthResponse with new tokens
     */
    public AuthResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        String ipAddress = getClientIP(httpRequest);

        // Rotate refresh token (verifies, revokes old, creates new)
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(
                request.getRefreshToken(),
                ipAddress
        );

        // Generate new access token
        User user = newRefreshToken.getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        String newAccessToken = jwtUtils.generateJwtToken(authentication);

        metricsService.recordSecurityEvent("token_refresh");

        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .user(convertToResponse(user))
                .build();
    }

    /**
     * Extract client IP address from HTTP request.
     * Checks common proxy headers first, falls back to remote address.
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs (client, proxy1, proxy2, ...)
            // Take the first one (original client)
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private UserResponse convertToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .enabled(user.getEnabled())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
