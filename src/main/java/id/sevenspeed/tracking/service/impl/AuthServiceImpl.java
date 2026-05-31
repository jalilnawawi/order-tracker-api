// service/impl/AuthServiceImpl.java
package id.sevenspeed.tracking.service.impl;

import id.sevenspeed.tracking.dto.request.auth.LoginRequest;
import id.sevenspeed.tracking.dto.request.auth.RefreshRequest;
import id.sevenspeed.tracking.dto.response.auth.LoginResponse;
import id.sevenspeed.tracking.dto.response.common.UserResponse;
import id.sevenspeed.tracking.entity.RefreshToken;
import id.sevenspeed.tracking.entity.User;
import id.sevenspeed.tracking.exception.InvalidTokenException;
import id.sevenspeed.tracking.repository.RefreshTokenRepository;
import id.sevenspeed.tracking.repository.UserRepository;
import id.sevenspeed.tracking.security.CustomUserDetails;
import id.sevenspeed.tracking.security.JwtUtil;
import id.sevenspeed.tracking.service.AuthService;
import id.sevenspeed.tracking.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshTokenValue = jwtUtil.generateRefreshToken();
        Instant refreshExpiry = jwtUtil.generateRefreshTokenExpiry();

        User user = userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiresAt(refreshExpiry)
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        return buildLoginResponse(accessToken, refreshTokenValue, userDetails, user);
    }

    @Override
    @Transactional
    public LoginResponse refresh(RefreshRequest request) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenAndRevokedFalse(request.getRefreshToken())
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found or already revoked"));

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("Refresh token has expired");
        }

        User user = refreshToken.getUser();
        CustomUserDetails userDetails = new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                null,
                user.getRole().getCode(),
                user.getDivision() != null ? user.getDivision().getId() : null,
                user.getIsActive()
        );

        String newAccessToken = jwtUtil.generateAccessToken(userDetails);

        return buildLoginResponse(newAccessToken, request.getRefreshToken(), userDetails, user);
    }

    @Override
    @Transactional
    public void logout() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        refreshTokenRepository.revokeAllByUserId(userDetails.getUserId());
    }

    private LoginResponse buildLoginResponse(String accessToken, String refreshToken,
                                             CustomUserDetails userDetails, User user) {
        UserResponse userResponse = UserResponse.builder()
                .id(userDetails.getUserId())
                .username(userDetails.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .roleCode(userDetails.getRoleCode())
                .divisionId(userDetails.getDivisionId())
                .divisionCode(user.getDivision() != null ? user.getDivision().getCode() : null)
                .divisionName(user.getDivision() != null ? user.getDivision().getName() : null)
                .customerType(user.getCustomerType())
                .institutionName(user.getInstitutionName())
                .isActive(user.getIsActive())
                .lastLoginAt(DateTimeUtil.format(user.getLastLoginAt()))
                .createdAt(DateTimeUtil.format(user.getCreatedAt()))
                .build();

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(3600L)
                .tokenType("Bearer")
                .user(userResponse)
                .build();
    }
}