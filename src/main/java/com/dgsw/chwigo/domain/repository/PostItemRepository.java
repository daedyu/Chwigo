package com.dgsw.chwigo.domain.repository;

import com.dgsw.chwigo.domain.entity.Post;
import com.dgsw.chwigo.domain.entity.PostItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostItemRepository extends JpaRepository<PostItem, Long> {
    List<PostItem> findByPost(Post post);
    void deleteByPost(Post post);
}
