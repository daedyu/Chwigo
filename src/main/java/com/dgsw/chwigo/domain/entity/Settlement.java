package com.dgsw.chwigo.domain.entity;

import com.dgsw.chwigo.domain.enums.SettlementStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participation_id", nullable = false, unique = true)
    private Participation participation;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus status;

    private LocalDateTime paidAt;

    @Builder
    public Settlement(Participation participation, BigDecimal amount) {
        this.participation = participation;
        this.amount = amount;
        this.status = SettlementStatus.PENDING;
    }

    public void markAsPaid() {
        this.status = SettlementStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }
}
