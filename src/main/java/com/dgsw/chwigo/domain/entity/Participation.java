package com.dgsw.chwigo.domain.entity;

import com.dgsw.chwigo.domain.enums.ParticipationStatus;
import com.dgsw.chwigo.domain.entity.Settlement;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "participations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipationStatus status;

    @OneToMany(mappedBy = "participation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParticipationItem> participationItems = new ArrayList<>();

    @OneToOne(mappedBy = "participation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Settlement settlement;

    @Builder
    public Participation(Post post, User user) {
        this.post = post;
        this.user = user;
        this.status = ParticipationStatus.PENDING;
    }

    public void approve() {
        this.status = ParticipationStatus.APPROVED;
    }

    public void reject() {
        this.status = ParticipationStatus.REJECTED;
    }

    public BigDecimal getTotalAmount() {
        return participationItems.stream()
                .map(ParticipationItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
