package com.dgsw.chwigo.controller.docs;

import com.dgsw.chwigo.dto.request.LoginRequest;
import com.dgsw.chwigo.dto.request.RefreshTokenRequest;
import com.dgsw.chwigo.dto.request.RegisterRequest;
import com.dgsw.chwigo.dto.request.UpdateProfileRequest;
import com.dgsw.chwigo.dto.response.TokenResponse;
import com.dgsw.chwigo.dto.response.UserResponse;
import com.dgsw.chwigo.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Auth", description = "인증 API (회원가입, 로그인, 토큰 갱신, 로그아웃, 프로필)")
public interface AuthDocument {

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임으로 신규 계정을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<com.dgsw.chwigo.dto.response.ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request);

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 AccessToken·RefreshToken을 발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<com.dgsw.chwigo.dto.response.ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request);

    @Operation(summary = "토큰 갱신", description = "RefreshToken으로 새로운 AccessToken·RefreshToken을 발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 RefreshToken",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<com.dgsw.chwigo.dto.response.ApiResponse<TokenResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request);

    @Operation(summary = "로그아웃", description = "서버에 저장된 RefreshToken을 삭제합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({@ApiResponse(responseCode = "200", description = "로그아웃 성공")})
    ResponseEntity<com.dgsw.chwigo.dto.response.ApiResponse<Void>> logout(
            @AuthenticationPrincipal UserPrincipal principal);

    @Operation(summary = "내 프로필 조회", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공")})
    ResponseEntity<com.dgsw.chwigo.dto.response.ApiResponse<UserResponse>> getMyProfile(
            @AuthenticationPrincipal UserPrincipal principal);

    @Operation(summary = "프로필 수정", description = "닉네임, 주소(동네)를 수정합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 오류",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<com.dgsw.chwigo.dto.response.ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserPrincipal principal);
}
