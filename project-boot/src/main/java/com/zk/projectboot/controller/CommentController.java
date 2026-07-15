package com.zk.projectboot.controller;

import com.zk.projectboot.application.CommentApplicationService;
import com.zk.projectboot.dto.ApiResponse;
import com.zk.projectboot.dto.CommentDTO;
import com.zk.projectboot.dto.CommentWriteRequest;
import com.zk.projectboot.exception.BusinessException;
import com.zk.projectboot.model.Comment;
import com.zk.projectboot.model.User;
import com.zk.projectboot.security.CurrentUserService;
import com.zk.projectboot.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;
    private final CommentApplicationService commentApplicationService;
    private final CurrentUserService currentUserService;

    public CommentController(CommentService commentService,
                             CommentApplicationService commentApplicationService,
                             CurrentUserService currentUserService) {
        this.commentService = commentService;
        this.commentApplicationService = commentApplicationService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CommentDTO>>> getAllComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<CommentDTO> comments = commentService.getAllComments(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(CommentDTO::fromComment);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CommentDTO>> getCommentById(@PathVariable Integer id) {
        Comment comment = commentService.getCommentById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "评论不存在"));
        if (comment.getStatus() != Comment.CommentStatus.approved) {
            Optional<User> actor = currentUserService.findCurrentUser();
            if (!actor.isPresent() || (!actor.get().getId().equals(comment.getUser().getId())
                    && !currentUserService.isAdmin(actor.get()))) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "评论不存在");
            }
        }
        return ResponseEntity.ok(ApiResponse.success(CommentDTO.fromComment(comment)));
    }

    @GetMapping("/article/{articleId}")
    public ResponseEntity<ApiResponse<List<CommentDTO>>> getCommentsByArticle(
            @PathVariable Integer articleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Comment> comments = commentService.getCommentsByArticleId(articleId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ResponseEntity.ok(ApiResponse.success(comments.getContent().stream()
                .map(CommentDTO::fromComment).collect(Collectors.toList())));
    }

    @GetMapping("/replies/{commentId}")
    public ResponseEntity<ApiResponse<List<CommentDTO>>> getReplies(@PathVariable Integer commentId) {
        return ResponseEntity.ok(ApiResponse.success(commentService.getRepliesByCommentId(commentId).stream()
                .map(CommentDTO::fromComment).collect(Collectors.toList())));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<CommentDTO>>> getCommentsByUser(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        currentUserService.requireSelfOrAdmin(userId);
        Page<CommentDTO> comments = commentService.getCommentsByUserId(userId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(CommentDTO::fromComment);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CommentDTO>> createComment(
            @Valid @RequestBody CommentWriteRequest request,
            @RequestParam Integer articleId,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer parentId,
            @RequestParam(required = false) Integer replyToUserId) {
        Comment comment = commentApplicationService.create(request, articleId, parentId, replyToUserId);
        return ResponseEntity.ok(ApiResponse.success(CommentDTO.fromComment(comment)));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<CommentDTO>> approve(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("评论已审核通过",
                CommentDTO.fromComment(commentApplicationService.approve(id))));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<CommentDTO>> reject(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("评论已拒绝",
                CommentDTO.fromComment(commentApplicationService.reject(id))));
    }

    @PatchMapping("/{id}/like")
    public ResponseEntity<ApiResponse<CommentDTO>> like(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("点赞成功",
                CommentDTO.fromComment(commentApplicationService.like(id))));
    }

    @PatchMapping("/{id}/unlike")
    public ResponseEntity<ApiResponse<CommentDTO>> unlike(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("取消点赞成功",
                CommentDTO.fromComment(commentApplicationService.unlike(id))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        commentApplicationService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("评论已删除", null));
    }
}
