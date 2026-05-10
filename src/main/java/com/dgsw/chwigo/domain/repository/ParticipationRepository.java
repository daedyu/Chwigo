package com.dgsw.chwigo.domain.repository;

import com.dgsw.chwigo.domain.entity.Participation;
import com.dgsw.chwigo.domain.entity.Post;
import com.dgsw.chwigo.domain.entity.User;
import com.dgsw.chwigo.domain.enums.ParticipationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    List<Participation> findByPost(Post post);
    List<Participation> findByUser(User user);
    Optional<Participation> findByPostAndUser(Post post, User user);
    boolean existsByPostAndUser(Post post, User user);

    @Query("SELECT p FROM Participation p WHERE p.post = :post AND p.status = :status")
    List<Participation> findByPostAndStatus(@Param("post") Post post, @Param("status") ParticipationStatus status);

    long countByPostAndStatus(Post post, ParticipationStatus status);

    boolean existsByPostAndStatusNot(Post post, ParticipationStatus status);

    void deleteByPost(Post post);
}
