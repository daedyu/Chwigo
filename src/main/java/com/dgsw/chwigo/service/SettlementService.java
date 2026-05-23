package com.dgsw.chwigo.service;

import com.dgsw.chwigo.domain.entity.Settlement;
import com.dgsw.chwigo.domain.entity.User;
import com.dgsw.chwigo.domain.enums.SettlementStatus;
import com.dgsw.chwigo.domain.repository.PostRepository;
import com.dgsw.chwigo.domain.repository.SettlementRepository;
import com.dgsw.chwigo.domain.repository.UserRepository;
import com.dgsw.chwigo.dto.response.SettlementResponse;
import com.dgsw.chwigo.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<SettlementResponse> getSettlementsByPost(Long postId, String email) {
        var post = postRepository.findById(postId)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다."));
        if (!post.getAuthor().getEmail().equals(email)) {
            throw CustomException.forbidden("권한이 없습니다.");
        }
        return settlementRepository.findByPostId(postId).stream()
                .map(SettlementResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SettlementResponse> getMySettlements(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> CustomException.notFound("사용자를 찾을 수 없습니다."));
        return settlementRepository.findByParticipationUser(user).stream()
                .map(SettlementResponse::from)
                .toList();
    }

    @Transactional
    public SettlementResponse markAsPaid(Long settlementId, String email) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> CustomException.notFound("정산 정보를 찾을 수 없습니다."));

        String postAuthorEmail = settlement.getParticipation().getPost().getAuthor().getEmail();
        if (!postAuthorEmail.equals(email)) {
            throw CustomException.forbidden("권한이 없습니다.");
        }
        if (settlement.getStatus() == SettlementStatus.PAID) {
            throw CustomException.badRequest("이미 정산이 완료된 건입니다.");
        }

        settlement.markAsPaid();
        return SettlementResponse.from(settlement);
    }
}
