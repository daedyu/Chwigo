package com.dgsw.chwigo.controller;

import com.dgsw.chwigo.controller.docs.AuthDocument;
import com.dgsw.chwigo.dto.request.LoginRequest;
import com.dgsw.chwigo.dto.request.RefreshTokenRequest;
import com.dgsw.chwigo.dto.request.RegisterRequest;
import com.dgsw.chwigo.dto.request.UpdateProfileRequest;
import com.dgsw.chwigo.dto.response.ApiResponse;
import com.dgsw.chwigo.dto.response.TokenResponse;
import com.dgsw.chwigo.dto.response.UserResponse;
import com.dgsw.chwigo.security.UserPrincipal;
import com.dgsw.chwigo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController implements AuthDocument {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("회원가입이 완료되었습니다.", authService.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.refresh(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal UserPrincipal principal) {
        authService.logout(principal.getEmail());
        return ResponseEntity.ok(ApiResponse.ok("로그아웃되었습니다."));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("내 정보를 조회했습니다.", authService.getMyProfile(principal.getEmail())));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("프로필이 수정되었습니다.", authService.updateProfile(principal.getEmail(), request)));
    }
}
