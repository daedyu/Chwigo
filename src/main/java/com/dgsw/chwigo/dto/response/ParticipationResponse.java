package com.dgsw.chwigo.dto.response;

import com.dgsw.chwigo.domain.entity.Participation;
import com.dgsw.chwigo.domain.enums.ParticipationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ParticipationResponse(
        Long id,
        Long postId,
        String postTitle,
        Long userId,
        String userNickname,
        ParticipationStatus status,
        List<ParticipationItemResponse> items,
        BigDecimal totalAmount,
        LocalDateTime createdAt
) {
    public static ParticipationResponse from(Participation p) {
        return new ParticipationResponse(
                p.getId(),
                p.getPost().getId(),
                p.getPost().getTitle(),
                p.getUser().getId(),
                p.getUser().getNickname(),
                p.getStatus(),
                p.getParticipationItems().stream().map(ParticipationItemResponse::from).toList(),
                p.getTotalAmount(),
                p.getCreatedAt()
        );
    }
}
