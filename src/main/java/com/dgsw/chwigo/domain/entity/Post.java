package com.dgsw.chwigo.domain.entity;

import com.dgsw.chwigo.domain.enums.Category;
import com.dgsw.chwigo.domain.enums.PostStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status;

    private String meetLocation;

    private LocalDateTime deadline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participation> participations = new ArrayList<>();

    @Builder
    public Post(String title, String description, Category category,
                String meetLocation, LocalDateTime deadline, User author) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.meetLocation = meetLocation;
        this.deadline = deadline;
        this.author = author;
        this.status = PostStatus.OPEN;
    }

    public void update(String title, String description, Category category,
                       String meetLocation, LocalDateTime deadline) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.meetLocation = meetLocation;
        this.deadline = deadline;
    }

    public void checkAndUpdateStatus() {
        if (this.status == PostStatus.CLOSED) return;
        boolean allFull = !items.isEmpty() && items.stream().allMatch(PostItem::isFull);
        this.status = allFull ? PostStatus.FULL : PostStatus.OPEN;
    }

    public void close() {
        this.status = PostStatus.CLOSED;
    }

    public int getTotalMaxParticipants() {
        return items.stream().mapToInt(PostItem::getMaxParticipants).max().orElse(0);
    }

    public boolean isOpen() {
        return this.status == PostStatus.OPEN;
    }
}
