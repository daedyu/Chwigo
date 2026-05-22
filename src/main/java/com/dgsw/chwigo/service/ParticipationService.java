package com.dgsw.chwigo.service;

import com.dgsw.chwigo.domain.entity.*;
import com.dgsw.chwigo.domain.enums.ParticipationStatus;
import com.dgsw.chwigo.domain.enums.PostStatus;
import com.dgsw.chwigo.domain.repository.*;
import com.dgsw.chwigo.dto.request.ParticipationItemRequest;
import com.dgsw.chwigo.dto.request.ParticipationRequest;
import com.dgsw.chwigo.dto.response.ParticipationResponse;
import com.dgsw.chwigo.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParticipationService {

    private final ParticipationRepository participationRepository;
    private final ParticipationItemRepository participationItemRepository;
    private final PostRepository postRepository;
    private final PostItemRepository postItemRepository;
    private final UserRepository userRepository;
    private final SettlementRepository settlementRepository;

    @Transactional
    public ParticipationResponse apply(Long postId, ParticipationRequest request, String email) {
        Post post = findPost(postId);
        User user = findUser(email);

        if (post.getAuthor().getEmail().equals(email)) {
            throw CustomException.badRequest("본인 게시글에는 참여할 수 없습니다.");
        }
        if (!post.isOpen()) {
            throw CustomException.badRequest("참여 신청이 불가능한 게시글입니다. (상태: " + post.getStatus() + ")");
        }
        participationRepository.findByPostAndUser(post, user).ifPresent(existing -> {
            if (existing.getStatus() == ParticipationStatus.REJECTED) {
                participationRepository.delete(existing);
                participationRepository.flush();
            } else {
                throw CustomException.conflict("이미 참여 신청한 게시글입니다.");
            }
        });

        List<PostItem> postItems = postItemRepository.findByPost(post);
        validateItemRequests(request.items(), postItems, post.getId());

        Participation participation = participationRepository.save(
                Participation.builder().post(post).user(user).build());

        request.items().forEach(itemReq -> {
            PostItem postItem = findPostItem(postItems, itemReq.postItemId());
            ParticipationItem pi = ParticipationItem.builder()
                    .participation(participation)
                    .postItem(postItem)
                    .quantity(itemReq.quantity())
                    .build();
            participationItemRepository.save(pi);
            participation.getParticipationItems().add(pi);
        });

        return ParticipationResponse.from(participation);
    }

    @Transactional(readOnly = true)
    public List<ParticipationResponse> getParticipations(Long postId, String email) {
        Post post = findPost(postId);
        if (!post.getAuthor().getEmail().equals(email)) {
            throw CustomException.forbidden("권한이 없습니다.");
        }
        return participationRepository.findByPost(post).stream()
                .map(ParticipationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ParticipationResponse> getMyParticipations(String email) {
        User user = findUser(email);
        return participationRepository.findByUser(user).stream()
                .map(ParticipationResponse::from)
                .toList();
    }

    @Transactional
    public ParticipationResponse approve(Long postId, Long participationId, String email) {
        Post post = findPost(postId);
        if (!post.getAuthor().getEmail().equals(email)) {
            throw CustomException.forbidden("권한이 없습니다.");
        }
        Participation participation = findParticipation(participationId);
        if (participation.getStatus() != ParticipationStatus.PENDING) {
            throw CustomException.badRequest("이미 처리된 참여 신청입니다.");
        }

        participation.getParticipationItems().forEach(pi ->
                pi.getPostItem().addParticipant(pi.getQuantity()));

        participation.approve();

        post.checkAndUpdateStatus();

        Settlement settlement = Settlement.builder()
                .participation(participation)
                .amount(participation.getTotalAmount())
                .build();
        settlementRepository.save(settlement);

        return ParticipationResponse.from(participation);
    }

    @Transactional
    public ParticipationResponse reject(Long postId, Long participationId, String email) {
        Post post = findPost(postId);
        if (!post.getAuthor().getEmail().equals(email)) {
            throw CustomException.forbidden("권한이 없습니다.");
        }
        Participation participation = findParticipation(participationId);
        if (participation.getStatus() != ParticipationStatus.PENDING) {
            throw CustomException.badRequest("이미 처리된 참여 신청입니다.");
        }
        participation.reject();
        return ParticipationResponse.from(participation);
    }

    @Transactional
    public void cancel(Long postId, Long participationId, String email) {
        findPost(postId);
        Participation participation = findParticipation(participationId);
        if (!participation.getUser().getEmail().equals(email)) {
            throw CustomException.forbidden("권한이 없습니다.");
        }
        if (participation.getStatus() == ParticipationStatus.APPROVED) {
            participation.getParticipationItems().forEach(pi ->
                    pi.getPostItem().removeParticipant(pi.getQuantity()));
            participation.getPost().checkAndUpdateStatus();
        }
        participationRepository.delete(participation);
    }

    private void validateItemRequests(List<ParticipationItemRequest> requests,
                                      List<PostItem> postItems, Long postId) {
        requests.forEach(req -> {
            PostItem item = postItems.stream()
                    .filter(pi -> pi.getId().equals(req.postItemId()))
                    .findFirst()
                    .orElseThrow(() -> CustomException.badRequest(
                            "품목 ID " + req.postItemId() + " 은(는) 이 게시글(ID: " + postId + ")의 품목이 아닙니다."));
            if (req.quantity() > item.getRemainingSlots()) {
                throw CustomException.badRequest(
                        "품목 '" + item.getName() + "'의 남은 자리(" + item.getRemainingSlots() + "자리)가 부족합니다.");
            }
        });
    }

    private PostItem findPostItem(List<PostItem> items, Long itemId) {
        return items.stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> CustomException.notFound("품목을 찾을 수 없습니다."));
    }

    private Post findPost(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다."));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> CustomException.notFound("사용자를 찾을 수 없습니다."));
    }

    private Participation findParticipation(Long id) {
        return participationRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("참여 신청을 찾을 수 없습니다."));
    }
}
