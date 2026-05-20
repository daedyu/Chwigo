package com.dgsw.chwigo.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PostItemRequest(
        @NotBlank(message = "품목명을 입력해주세요") String name,
        @NotNull @DecimalMin(value = "0", inclusive = false, message = "금액은 0보다 커야 합니다") BigDecimal totalPrice,
        @Min(value = 2, message = "품목당 최대 참여 인원은 2명 이상이어야 합니다") int maxParticipants
) {}
