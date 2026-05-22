package com.dgsw.chwigo.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ParticipationRequest(
        @NotEmpty(message = "참여할 품목을 선택해주세요") @Valid List<ParticipationItemRequest> items
) {}
