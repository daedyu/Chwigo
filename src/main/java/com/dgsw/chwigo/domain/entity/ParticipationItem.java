package com.dgsw.chwigo.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "participation_items",
        uniqueConstraints = @UniqueConstraint(columnNames = {"participation_id", "post_item_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParticipationItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participation_id", nullable = false)
    private Participation participation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_item_id", nullable = false)
    private PostItem postItem;

    private int quantity;

    @Builder
    public ParticipationItem(Participation participation, PostItem postItem, int quantity) {
        this.participation = participation;
        this.postItem = postItem;
        this.quantity = quantity;
    }

    public BigDecimal getAmount() {
        return postItem.getUnitPrice().multiply(BigDecimal.valueOf(quantity));
    }
}
