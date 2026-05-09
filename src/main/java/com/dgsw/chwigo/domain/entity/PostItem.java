package com.dgsw.chwigo.domain.entity;

import com.dgsw.chwigo.exception.CustomException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "post_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false)
    private String name;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private int maxParticipants;

    private int currentParticipants;

    @Builder
    public PostItem(Post post, String name, BigDecimal totalPrice, int maxParticipants) {
        this.post = post;
        this.name = name;
        this.totalPrice = totalPrice;
        this.maxParticipants = maxParticipants;
        this.currentParticipants = 0;
    }

    public BigDecimal getUnitPrice() {
        return totalPrice.divide(BigDecimal.valueOf(maxParticipants), 2, RoundingMode.HALF_UP);
    }

    public boolean isFull() {
        return currentParticipants >= maxParticipants;
    }

    public int getRemainingSlots() {
        return maxParticipants - currentParticipants;
    }

    public void addParticipant(int quantity) {
        if (currentParticipants + quantity > maxParticipants) {
            throw CustomException.badRequest(
                    "품목 '" + name + "'의 최대 참여 인원(" + maxParticipants + "명)을 초과했습니다. " +
                    "현재 남은 자리: " + getRemainingSlots() + "자리");
        }
        this.currentParticipants += quantity;
    }

    public void removeParticipant(int quantity) {
        this.currentParticipants = Math.max(0, this.currentParticipants - quantity);
    }

    public void update(String name, BigDecimal totalPrice, int maxParticipants) {
        if (maxParticipants < this.currentParticipants) {
            throw CustomException.badRequest(
                    "최대 참여 인원(" + maxParticipants + "명)이 현재 참여 인원(" + currentParticipants + "명)보다 적을 수 없습니다.");
        }
        this.name = name;
        this.totalPrice = totalPrice;
        this.maxParticipants = maxParticipants;
    }
}
