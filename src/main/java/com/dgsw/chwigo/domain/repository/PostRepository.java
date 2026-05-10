package com.dgsw.chwigo.domain.repository;

import com.dgsw.chwigo.domain.entity.Post;
import com.dgsw.chwigo.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    List<Post> findByAuthorOrderByCreatedAtDesc(User author);

    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.items WHERE p.id = :id")
    Optional<Post> findWithItemsById(@Param("id") Long id);
}
