package com.dgsw.chwigo.dto.request;

import com.dgsw.chwigo.domain.enums.Category;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record PostCreateRequest(
        @NotBlank(message = "제목을 입력해주세요") String title,
        @NotBlank(message = "내용을 입력해주세요") String description,
        @NotNull(message = "카테고리를 선택해주세요") Category category,
        @NotBlank(message = "만날 장소를 입력해주세요") String meetLocation,
        @NotNull(message = "마감 시간을 입력해주세요") @Future(message = "마감 시간은 현재 시간 이후여야 합니다") LocalDateTime deadline,
        @NotEmpty(message = "품목을 최소 1개 이상 입력해주세요") @Valid List<PostItemRequest> items
) {}
