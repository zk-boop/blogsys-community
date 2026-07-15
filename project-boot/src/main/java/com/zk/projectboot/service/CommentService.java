package com.zk.projectboot.service;

import com.zk.projectboot.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CommentService {

    Comment saveComment(Comment comment);

    Optional<Comment> getCommentById(Integer id);

    Page<Comment> getCommentsByArticleId(Integer articleId, Pageable pageable);

    List<Comment> getRepliesByCommentId(Integer commentId);

    Page<Comment> getCommentsByUserId(Integer userId, Pageable pageable);

    Page<Comment> getAllComments(Pageable pageable);

    Comment updateComment(Comment comment);

    boolean approveComment(Integer id);

    boolean rejectComment(Integer id);

    boolean deleteComment(Integer id);

    boolean incrementLikeCount(Integer id);

    boolean decrementLikeCount(Integer id);

    long countByArticleId(Integer articleId);
}
