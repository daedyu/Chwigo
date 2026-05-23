package com.dgsw.chwigo.controller;

import com.dgsw.chwigo.controller.docs.SettlementDocument;
import com.dgsw.chwigo.dto.response.ApiResponse;
import com.dgsw.chwigo.dto.response.SettlementResponse;
import com.dgsw.chwigo.security.UserPrincipal;
import com.dgsw.chwigo.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SettlementController implements SettlementDocument {

    private final SettlementService settlementService;

    @GetMapping("/posts/{postId}/settlements")
    public ResponseEntity<ApiResponse<List<SettlementResponse>>> getSettlementsByPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                settlementService.getSettlementsByPost(postId, principal.getEmail())));
    }

    @GetMapping("/my/settlements")
    public ResponseEntity<ApiResponse<List<SettlementResponse>>> getMySettlements(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                settlementService.getMySettlements(principal.getEmail())));
    }

    @PutMapping("/settlements/{id}/pay")
    public ResponseEntity<ApiResponse<SettlementResponse>> markAsPaid(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("정산이 완료 처리되었습니다.",
                settlementService.markAsPaid(id, principal.getEmail())));
    }
}
