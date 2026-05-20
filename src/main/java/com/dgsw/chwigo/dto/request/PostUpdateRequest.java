package com.dgsw.chwigo.dto.request;

import com.dgsw.chwigo.domain.enums.Category;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record PostUpdateRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotNull Category category,
        @NotBlank String meetLocation,
        @NotNull @Future LocalDateTime deadline,
        @NotEmpty @Valid List<PostItemRequest> items
) {}
