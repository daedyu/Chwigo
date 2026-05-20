package com.dgsw.chwigo.dto.response;

import com.dgsw.chwigo.domain.entity.Post;
import com.dgsw.chwigo.domain.enums.Category;
import com.dgsw.chwigo.domain.enums.PostStatus;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
        Long id,
        String title,
        String description,
        Category category,
        PostStatus status,
        String meetLocation,
        LocalDateTime deadline,
        Long authorId,
        String authorNickname,
        List<PostItemResponse> items,
        int totalMaxParticipants,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getDescription(),
                post.getCategory(),
                post.getStatus(),
                post.getMeetLocation(),
                post.getDeadline(),
                post.getAuthor().getId(),
                post.getAuthor().getNickname(),
                post.getItems().stream().map(PostItemResponse::from).toList(),
                post.getTotalMaxParticipants(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
