package com.dgsw.chwigo.dto.response;

import com.dgsw.chwigo.domain.entity.PostItem;

import java.math.BigDecimal;

public record PostItemResponse(
        Long id,
        String name,
        BigDecimal totalPrice,
        BigDecimal unitPrice,
        int maxParticipants,
        int currentParticipants,
        int remainingSlots,
        boolean full
) {
    public static PostItemResponse from(PostItem item) {
        return new PostItemResponse(
                item.getId(),
                item.getName(),
                item.getTotalPrice(),
                item.getUnitPrice(),
                item.getMaxParticipants(),
                item.getCurrentParticipants(),
                item.getRemainingSlots(),
                item.isFull()
        );
    }
}
