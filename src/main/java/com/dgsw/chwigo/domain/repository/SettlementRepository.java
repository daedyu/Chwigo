package com.dgsw.chwigo.domain.repository;

import com.dgsw.chwigo.domain.entity.Participation;
import com.dgsw.chwigo.domain.entity.Settlement;
import com.dgsw.chwigo.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    Optional<Settlement> findByParticipation(Participation participation);

    @Query("SELECT s FROM Settlement s JOIN s.participation p WHERE p.user = :user")
    List<Settlement> findByParticipationUser(@Param("user") User user);

    @Query("SELECT s FROM Settlement s JOIN s.participation p WHERE p.post.id = :postId")
    List<Settlement> findByPostId(@Param("postId") Long postId);
}
