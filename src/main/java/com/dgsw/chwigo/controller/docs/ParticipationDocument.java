package com.dgsw.chwigo.controller.docs;

import com.dgsw.chwigo.dto.request.ParticipationRequest;
import com.dgsw.chwigo.dto.response.ParticipationResponse;
import com.dgsw.chwigo.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Participations", description = "공동구매 참여 신청 API")
public interface ParticipationDocument {

    @Operation(summary = "참여 신청",
            description = "원하는 품목(postItemId)과 수량(quantity)을 선택해서 신청합니다. " +
                    "수량은 해당 품목의 남은 자리를 초과할 수 없습니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "참여 신청 성공"),
            @ApiResponse(responseCode = "400", description = "본인 게시글·모집 마감·수량 초과",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "이미 참여 신청함",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<com.dgsw.chwigo.dto.response.ApiResponse<ParticipationResponse>> apply(
            @Parameter(description = "게시글 ID") @PathVariable Long postId,
            @Valid @RequestBody ParticipationRequest request,
            @AuthenticationPrincipal UserPrincipal principal);

    @Operation(summary = "참여자 목록 조회", description = "게시글 작성자만 조회 가능합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<com.dgsw.chwigo.dto.response.ApiResponse<List<ParticipationResponse>>> getParticipations(
            @Parameter(description = "게시글 ID") @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal principal);

    @Operation(summary = "참여 승인",
            description = "승인 시 품목별 자리가 확보되고 정산 정보가 자동 생성됩니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "승인 성공"),
            @ApiResponse(responseCode = "400", description = "이미 처리된 신청 또는 품목 자리 부족",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<com.dgsw.chwigo.dto.response.ApiResponse<ParticipationResponse>> approve(
            @Parameter(description = "게시글 ID") @PathVariable Long postId,
            @Parameter(description = "참여 신청 ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal);

    @Operation(summary = "참여 거절", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "거절 성공"),
            @ApiResponse(responseCode = "400", description = "이미 처리된 신청",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<com.dgsw.chwigo.dto.response.ApiResponse<ParticipationResponse>> reject(
            @Parameter(description = "게시글 ID") @PathVariable Long postId,
            @Parameter(description = "참여 신청 ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal);

    @Operation(summary = "참여 취소", description = "승인된 참여를 취소하면 품목 자리가 반환됩니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "취소 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<com.dgsw.chwigo.dto.response.ApiResponse<Void>> cancel(
            @Parameter(description = "게시글 ID") @PathVariable Long postId,
            @Parameter(description = "참여 신청 ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal);

    @Operation(summary = "내 참여 목록 조회", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공")})
    ResponseEntity<com.dgsw.chwigo.dto.response.ApiResponse<List<ParticipationResponse>>> getMyParticipations(
            @AuthenticationPrincipal UserPrincipal principal);
}
