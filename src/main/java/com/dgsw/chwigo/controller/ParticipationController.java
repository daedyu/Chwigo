package com.dgsw.chwigo.controller;

import com.dgsw.chwigo.controller.docs.ParticipationDocument;
import com.dgsw.chwigo.dto.request.ParticipationRequest;
import com.dgsw.chwigo.dto.response.ApiResponse;
import com.dgsw.chwigo.dto.response.ParticipationResponse;
import com.dgsw.chwigo.security.UserPrincipal;
import com.dgsw.chwigo.service.ParticipationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ParticipationController implements ParticipationDocument {

    private final ParticipationService participationService;

    @PostMapping("/posts/{postId}/participations")
    public ResponseEntity<ApiResponse<ParticipationResponse>> apply(
            @PathVariable Long postId,
            @Valid @RequestBody ParticipationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("참여 신청이 완료되었습니다.",
                        participationService.apply(postId, request, principal.getEmail())));
    }

    @GetMapping("/posts/{postId}/participations")
    public ResponseEntity<ApiResponse<List<ParticipationResponse>>> getParticipations(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                participationService.getParticipations(postId, principal.getEmail())));
    }

    @PutMapping("/posts/{postId}/participations/{id}/approve")
    public ResponseEntity<ApiResponse<ParticipationResponse>> approve(
            @PathVariable Long postId,
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("참여가 승인되었습니다.",
                participationService.approve(postId, id, principal.getEmail())));
    }

    @PutMapping("/posts/{postId}/participations/{id}/reject")
    public ResponseEntity<ApiResponse<ParticipationResponse>> reject(
            @PathVariable Long postId,
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("참여가 거절되었습니다.",
                participationService.reject(postId, id, principal.getEmail())));
    }

    @DeleteMapping("/posts/{postId}/participations/{id}")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @PathVariable Long postId,
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        participationService.cancel(postId, id, principal.getEmail());
        return ResponseEntity.ok(ApiResponse.ok("참여 신청이 취소되었습니다."));
    }

    @GetMapping("/my/participations")
    public ResponseEntity<ApiResponse<List<ParticipationResponse>>> getMyParticipations(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                participationService.getMyParticipations(principal.getEmail())));
    }
}
