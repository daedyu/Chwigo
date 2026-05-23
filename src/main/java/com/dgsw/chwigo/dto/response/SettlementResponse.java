package com.dgsw.chwigo.dto.response;

import com.dgsw.chwigo.domain.entity.Settlement;
import com.dgsw.chwigo.domain.enums.SettlementStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SettlementResponse(
        Long id, Long participationId,
        Long postId, String postTitle,
        String participantNickname,
        BigDecimal amount, SettlementStatus status,
        LocalDateTime paidAt, LocalDateTime createdAt
) {
    public static SettlementResponse from(Settlement s) {
        return new SettlementResponse(
                s.getId(), s.getParticipation().getId(),
                s.getParticipation().getPost().getId(),
                s.getParticipation().getPost().getTitle(),
                s.getParticipation().getUser().getNickname(),
                s.getAmount(), s.getStatus(),
                s.getPaidAt(), s.getCreatedAt()
        );
    }
}
