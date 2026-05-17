package com.dgsw.chwigo.service;

import com.dgsw.chwigo.domain.entity.User;
import com.dgsw.chwigo.domain.enums.UserRole;
import com.dgsw.chwigo.domain.repository.UserRepository;
import com.dgsw.chwigo.dto.request.LoginRequest;
import com.dgsw.chwigo.dto.request.RefreshTokenRequest;
import com.dgsw.chwigo.dto.request.RegisterRequest;
import com.dgsw.chwigo.dto.request.UpdateProfileRequest;
import com.dgsw.chwigo.dto.response.TokenResponse;
import com.dgsw.chwigo.dto.response.UserResponse;
import com.dgsw.chwigo.exception.CustomException;
import com.dgsw.chwigo.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw CustomException.conflict("이미 사용 중인 이메일입니다.");
        }
        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .address(request.address())
                .role(UserRole.ROLE_USER)
                .build();
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> CustomException.unauthorized("이메일 또는 비밀번호가 올바르지 않습니다."));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw CustomException.unauthorized("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        String accessToken = jwtProvider.generateAccessToken(user.getEmail());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());
        user.updateRefreshToken(refreshToken);
        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        if (!jwtProvider.isValid(request.refreshToken())) {
            throw CustomException.unauthorized("유효하지 않은 리프레시 토큰입니다.");
        }
        User user = userRepository.findByRefreshToken(request.refreshToken())
                .orElseThrow(() -> CustomException.unauthorized("리프레시 토큰이 만료되었습니다."));
        String newAccessToken = jwtProvider.generateAccessToken(user.getEmail());
        String newRefreshToken = jwtProvider.generateRefreshToken(user.getEmail());
        user.updateRefreshToken(newRefreshToken);
        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String email) {
        userRepository.findByEmail(email)
                .ifPresent(user -> user.updateRefreshToken(null));
    }

    @Transactional(readOnly = true)
    public UserResponse getMyProfile(String email) {
        return UserResponse.from(findUser(email));
    }

    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = findUser(email);
        user.updateProfile(request.nickname(), request.address());
        return UserResponse.from(user);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> CustomException.notFound("사용자를 찾을 수 없습니다."));
    }
}
