package com.dgsw.chwigo.domain.repository;

import com.dgsw.chwigo.domain.entity.Post;
import com.dgsw.chwigo.domain.enums.Category;
import com.dgsw.chwigo.domain.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {
    Page<Post> search(String keyword, Category category, PostStatus status, Pageable pageable);
}
