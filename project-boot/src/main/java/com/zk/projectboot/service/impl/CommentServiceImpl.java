package com.zk.projectboot.service.impl;

import com.zk.projectboot.model.Article;
import com.zk.projectboot.model.Comment;
import com.zk.projectboot.model.User;
import com.zk.projectboot.repository.ArticleRepository;
import com.zk.projectboot.repository.CommentRepository;
import com.zk.projectboot.repository.UserRepository;
import com.zk.projectboot.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository,
                              ArticleRepository articleRepository,
                              UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Comment saveComment(Comment comment) {
        return commentRepository.save(comment);
    }

    @Override
    public Optional<Comment> getCommentById(Integer id) {
        return commentRepository.findById(id);
    }

    @Override
    public Page<Comment> getCommentsByArticleId(Integer articleId, Pageable pageable) {
        Optional<Article> articleOptional = articleRepository.findById(articleId);
        if (articleOptional.isPresent()) {
            return commentRepository.findByArticleAndParentIsNullAndStatus(
                articleOptional.get(), Comment.CommentStatus.approved, pageable);
        }
        return Page.empty(pageable);
    }

    @Override
    public List<Comment> getRepliesByCommentId(Integer commentId) {
        return commentRepository.findByParentIdAndStatus(commentId, Comment.CommentStatus.approved);
    }

    @Override
    public Page<Comment> getCommentsByUserId(Integer userId, Pageable pageable) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            return commentRepository.findByUser(userOptional.get(), pageable);
        }
        return Page.empty(pageable);
    }

    @Override
    public Page<Comment> getAllComments(Pageable pageable) {
        return commentRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Comment updateComment(Comment comment) {
        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public boolean approveComment(Integer id) {
        Optional<Comment> commentOptional = commentRepository.findById(id);
        if (commentOptional.isPresent()) {
            Comment comment = commentOptional.get();
            comment.setStatus(Comment.CommentStatus.approved);
            commentRepository.save(comment);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean rejectComment(Integer id) {
        Optional<Comment> commentOptional = commentRepository.findById(id);
        if (commentOptional.isPresent()) {
            Comment comment = commentOptional.get();
            comment.setStatus(Comment.CommentStatus.rejected);
            commentRepository.save(comment);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deleteComment(Integer id) {
        if (commentRepository.existsById(id)) {
            commentRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean incrementLikeCount(Integer id) {
        Optional<Comment> commentOptional = commentRepository.findById(id);
        if (commentOptional.isPresent()) {
            Comment comment = commentOptional.get();
            comment.setLikeCount(comment.getLikeCount() + 1);
            commentRepository.save(comment);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean decrementLikeCount(Integer id) {
        Optional<Comment> commentOptional = commentRepository.findById(id);
        if (commentOptional.isPresent()) {
            Comment comment = commentOptional.get();
            // 确保点赞数不会小于0
            int likeCount = Math.max(0, comment.getLikeCount() - 1);
            comment.setLikeCount(likeCount);
            commentRepository.save(comment);
            return true;
        }
        return false;
    }

    @Override
    public long countByArticleId(Integer articleId) {
        Optional<Article> articleOptional = articleRepository.findById(articleId);
        if (articleOptional.isPresent()) {
            return commentRepository.countByArticleAndStatus(articleOptional.get(), Comment.CommentStatus.approved);
        }
        return 0;
    }
}
