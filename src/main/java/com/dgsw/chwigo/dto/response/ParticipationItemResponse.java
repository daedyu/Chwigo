package com.dgsw.chwigo.dto.response;

import com.dgsw.chwigo.domain.entity.ParticipationItem;

import java.math.BigDecimal;

public record ParticipationItemResponse(
        Long postItemId,
        String itemName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal amount
) {
    public static ParticipationItemResponse from(ParticipationItem pi) {
        return new ParticipationItemResponse(
                pi.getPostItem().getId(),
                pi.getPostItem().getName(),
                pi.getPostItem().getUnitPrice(),
                pi.getQuantity(),
                pi.getAmount()
        );
    }
}
