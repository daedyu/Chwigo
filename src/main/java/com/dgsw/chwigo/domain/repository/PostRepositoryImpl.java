package com.dgsw.chwigo.domain.repository;

import com.dgsw.chwigo.domain.entity.Post;
import com.dgsw.chwigo.domain.entity.QPost;
import com.dgsw.chwigo.domain.enums.Category;
import com.dgsw.chwigo.domain.enums.PostStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Post> search(String keyword, Category category, PostStatus status, Pageable pageable) {
        QPost post = QPost.post;

        List<Post> content = queryFactory
                .selectFrom(post)
                .where(
                        keywordContains(keyword),
                        categoryEq(category),
                        statusEq(status)
                )
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(post.count())
                .from(post)
                .where(
                        keywordContains(keyword),
                        categoryEq(category),
                        statusEq(status)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanExpression keywordContains(String keyword) {
        if (!StringUtils.hasText(keyword)) return null;
        QPost post = QPost.post;
        return post.title.containsIgnoreCase(keyword)
                .or(post.description.containsIgnoreCase(keyword));
    }

    private BooleanExpression categoryEq(Category category) {
        if (category == null) return null;
        return QPost.post.category.eq(category);
    }

    private BooleanExpression statusEq(PostStatus status) {
        if (status == null) return null;
        return QPost.post.status.eq(status);
    }
}
