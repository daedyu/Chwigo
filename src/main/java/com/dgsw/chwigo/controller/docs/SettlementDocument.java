package com.dgsw.chwigo.controller.docs;

import com.dgsw.chwigo.dto.response.SettlementResponse;
import com.dgsw.chwigo.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Tag(name = "Settlements", description = "정산 API")
public interface SettlementDocument {

    @Operation(summary = "게시글 정산 목록 조회",
            description = "해당 게시글의 모든 정산 정보를 조회합니다. 게시글 작성자만 가능.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<com.dgsw.chwigo.dto.response.ApiResponse<List<SettlementResponse>>> getSettlementsByPost(
            @Parameter(description = "게시글 ID") @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal principal);

    @Operation(summary = "내 정산 목록 조회", description = "내가 참여한 공동구매의 정산 목록을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<com.dgsw.chwigo.dto.response.ApiResponse<List<SettlementResponse>>> getMySettlements(
            @AuthenticationPrincipal UserPrincipal principal);

    @Operation(summary = "정산 완료 처리",
            description = "참여자의 정산을 완료 처리합니다. 게시글 작성자만 가능.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정산 완료 처리 성공"),
            @ApiResponse(responseCode = "400", description = "이미 정산 완료된 건",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<com.dgsw.chwigo.dto.response.ApiResponse<SettlementResponse>> markAsPaid(
            @Parameter(description = "정산 ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal);
}
