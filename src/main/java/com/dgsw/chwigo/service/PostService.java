package com.dgsw.chwigo.service;

import com.dgsw.chwigo.domain.entity.Post;
import com.dgsw.chwigo.domain.entity.PostItem;
import com.dgsw.chwigo.domain.entity.User;
import com.dgsw.chwigo.domain.enums.Category;
import com.dgsw.chwigo.domain.enums.PostStatus;
import com.dgsw.chwigo.domain.repository.PostItemRepository;
import com.dgsw.chwigo.domain.repository.PostRepository;
import com.dgsw.chwigo.domain.repository.UserRepository;
import com.dgsw.chwigo.dto.request.PostCreateRequest;
import com.dgsw.chwigo.dto.request.PostItemRequest;
import com.dgsw.chwigo.dto.request.PostUpdateRequest;
import com.dgsw.chwigo.dto.response.PostResponse;
import com.dgsw.chwigo.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostItemRepository postItemRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<PostResponse> getPosts(String keyword, Category category, PostStatus status, Pageable pageable) {
        return postRepository.search(keyword, category, status, pageable).map(PostResponse::from);
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(Long id) {
        return PostResponse.from(findPostWithItems(id));
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getMyPosts(String email) {
        User author = findUser(email);
        return postRepository.findByAuthorOrderByCreatedAtDesc(author).stream()
                .map(PostResponse::from)
                .toList();
    }

    @Transactional
    public PostResponse createPost(PostCreateRequest request, String email) {
        User author = findUser(email);
        Post post = Post.builder()
                .title(request.title())
                .description(request.description())
                .category(request.category())
                .meetLocation(request.meetLocation())
                .deadline(request.deadline())
                .author(author)
                .build();
        postRepository.save(post);
        saveItems(post, request.items());
        return PostResponse.from(post);
    }

    @Transactional
    public PostResponse updatePost(Long id, PostUpdateRequest request, String email) {
        Post post = findPostWithItems(id);
        validateAuthor(post, email);
        if (post.getStatus() == PostStatus.CLOSED) {
            throw CustomException.badRequest("마감된 게시글은 수정할 수 없습니다.");
        }
        post.update(request.title(), request.description(), request.category(),
                request.meetLocation(), request.deadline());

        postItemRepository.deleteByPost(post);
        post.getItems().clear();
        saveItems(post, request.items());
        return PostResponse.from(post);
    }

    @Transactional
    public void deletePost(Long id, String email) {
        Post post = findPost(id);
        validateAuthor(post, email);
        postRepository.delete(post);
    }

    @Transactional
    public PostResponse closePost(Long id, String email) {
        Post post = findPost(id);
        validateAuthor(post, email);
        if (post.getStatus() == PostStatus.CLOSED) {
            throw CustomException.badRequest("이미 마감된 게시글입니다.");
        }
        post.close();
        return PostResponse.from(post);
    }

    private void saveItems(Post post, List<PostItemRequest> itemRequests) {
        itemRequests.forEach(req -> {
            PostItem item = PostItem.builder()
                    .post(post)
                    .name(req.name())
                    .totalPrice(req.totalPrice())
                    .maxParticipants(req.maxParticipants())
                    .build();
            postItemRepository.save(item);
            post.getItems().add(item);
        });
    }

    private Post findPost(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다."));
    }

    private Post findPostWithItems(Long id) {
        return postRepository.findWithItemsById(id)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다."));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> CustomException.notFound("사용자를 찾을 수 없습니다."));
    }

    private void validateAuthor(Post post, String email) {
        if (!post.getAuthor().getEmail().equals(email)) {
            throw CustomException.forbidden("해당 게시글에 대한 권한이 없습니다.");
        }
    }
}
