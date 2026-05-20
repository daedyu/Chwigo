package com.dgsw.chwigo.controller.docs;

import com.dgsw.chwigo.domain.enums.Category;
import com.dgsw.chwigo.domain.enums.PostStatus;
import com.dgsw.chwigo.dto.request.PostCreateRequest;
import com.dgsw.chwigo.dto.request.PostUpdateRequest;
import com.dgsw.chwigo.dto.response.PostResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Posts", description = "공동구매 게시글 API (품목 포함)")
public interface PostDocument {

    @Operation(summary = "게시글 목록 조회",
            description = "keyword(제목/내용), category, status로 동적 필터링. 각 게시글에 품목 목록 포함.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공")})
    ResponseEntity<com.dgsw.chwigo.dto.response.ApiResponse<Page<PostResponse>>> getPosts(
            @Parameter(description = "검색 키워드") @RequestParam(required = false) String keyword,
            @Parameter(description = "카테고리: FOOD, DAILY, ELECTRONICS, CLOTHING, OTHER") @RequestParam(required = false) Category category,
            @Parameter(description = "상태: OPEN(모집중), FULL(모집완료), CLOSED(거래완료)") @RequestParam(required = false) PostStatus status,
            Pageable pageable);

    @Operation(summary = "게시글 상세 조회", description = "품목별 단가, 남은 자리 수 포함.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "게시글 없음",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<com.dgsw.chwigo.dto.response.ApiResponse<PostResponse>> getPost(
            @Parameter(description = "게시글 ID") @PathVariable Long id);

    @Operation(summary = "내 게시글 목록", description = "내가 작성한 게시글을 최신순으로 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공")})
    ResponseEntity<com.dgsw.chwigo.dto.response.ApiResponse<List<PostResponse>>> getMyPosts(
            @AuthenticationPrincipal UserPrincipal principal);

    @Operation(summary = "게시글 작성",
            description = "품목(items)을 최소 1개 이상 포함해야 합니다. 각 품목에 totalPrice와 maxParticipants를 지정합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "게시글 등록 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 오류",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<com.dgsw.chwigo.dto.response.ApiResponse<PostResponse>> createPost(
            @Valid @RequestBody PostCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal);

    @Operation(summary = "게시글 수정",
            description = "품목 목록을 교체합니다 (기존 품목 전체 삭제 후 재등록). 마감된 게시글 수정 불가.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<com.dgsw.chwigo.dto.response.ApiResponse<PostResponse>> updatePost(
            @Parameter(description = "게시글 ID") @PathVariable Long id,
            @Valid @RequestBody PostUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal);

    @Operation(summary = "게시글 삭제", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<com.dgsw.chwigo.dto.response.ApiResponse<Void>> deletePost(
            @Parameter(description = "게시글 ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal);

    @Operation(summary = "게시글 마감", description = "작성자가 수동으로 거래완료 처리합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "마감 성공"),
            @ApiResponse(responseCode = "400", description = "이미 마감된 게시글",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<com.dgsw.chwigo.dto.response.ApiResponse<PostResponse>> closePost(
            @Parameter(description = "게시글 ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal);
}
