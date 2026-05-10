package com.dgsw.chwigo.domain.repository;

import com.dgsw.chwigo.domain.entity.Participation;
import com.dgsw.chwigo.domain.entity.ParticipationItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipationItemRepository extends JpaRepository<ParticipationItem, Long> {
    List<ParticipationItem> findByParticipation(Participation participation);
}
