package com.dgsw.chwigo.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
        @NotBlank(message = "닉네임을 입력해주세요") String nickname,
        String address
) {}
