package com.dgsw.chwigo.controller;

import com.dgsw.chwigo.controller.docs.PostDocument;
import com.dgsw.chwigo.domain.enums.Category;
import com.dgsw.chwigo.domain.enums.PostStatus;
import com.dgsw.chwigo.dto.request.PostCreateRequest;
import com.dgsw.chwigo.dto.request.PostUpdateRequest;
import com.dgsw.chwigo.dto.response.ApiResponse;
import com.dgsw.chwigo.dto.response.PostResponse;
import com.dgsw.chwigo.security.UserPrincipal;
import com.dgsw.chwigo.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController implements PostDocument {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) PostStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(postService.getPosts(keyword, category, status, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(postService.getPost(id)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getMyPosts(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(postService.getMyPosts(principal.getEmail())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @Valid @RequestBody PostCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("게시글이 등록되었습니다.", postService.createPost(request, principal.getEmail())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("게시글이 수정되었습니다.", postService.updatePost(id, request, principal.getEmail())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        postService.deletePost(id, principal.getEmail());
        return ResponseEntity.ok(ApiResponse.ok("게시글이 삭제되었습니다."));
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<ApiResponse<PostResponse>> closePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("게시글이 마감되었습니다.", postService.closePost(id, principal.getEmail())));
    }
}
