package com.zk.projectboot.application;

import com.zk.projectboot.dto.CommentWriteRequest;
import com.zk.projectboot.exception.BusinessException;
import com.zk.projectboot.model.Article;
import com.zk.projectboot.model.Comment;
import com.zk.projectboot.model.User;
import com.zk.projectboot.repository.ArticleRepository;
import com.zk.projectboot.repository.CommentRepository;
import com.zk.projectboot.repository.UserRepository;
import com.zk.projectboot.security.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentApplicationService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public CommentApplicationService(CommentRepository commentRepository,
                                     ArticleRepository articleRepository,
                                     UserRepository userRepository,
                                     CurrentUserService currentUserService) {
        this.commentRepository = commentRepository;
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public Comment create(CommentWriteRequest request, Integer articleId,
                          Integer parentId, Integer replyToUserId) {
        User actor = currentUserService.requireCurrentUser();
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "文章不存在"));
        if (article.getStatus() != Article.ArticleStatus.published) {
            throw new BusinessException(HttpStatus.CONFLICT, "只能评论已发布文章");
        }

        Comment parent = null;
        if (parentId != null) {
            parent = requireComment(parentId);
            if (!parent.getArticle().getId().equals(articleId)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "父评论不属于当前文章");
            }
        }

        User replyTo = null;
        if (replyToUserId != null) {
            replyTo = userRepository.findById(replyToUserId)
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "回复用户不存在"));
        } else if (parent != null) {
            replyTo = parent.getUser();
        }

        Comment comment = new Comment();
        comment.setContent(request.getContent().trim());
        comment.setArticle(article);
        comment.setUser(actor);
        comment.setParent(parent);
        comment.setReplyToUser(replyTo);
        comment.setStatus(Comment.CommentStatus.approved);
        Comment saved = commentRepository.save(comment);
        articleRepository.incrementCommentCount(articleId);
        return saved;
    }

    @Transactional
    public Comment approve(Integer id) {
        currentUserService.requireAdmin();
        Comment comment = requireComment(id);
        if (comment.getStatus() != Comment.CommentStatus.approved) {
            comment.setStatus(Comment.CommentStatus.approved);
            articleRepository.incrementCommentCount(comment.getArticle().getId());
        }
        return commentRepository.save(comment);
    }

    @Transactional
    public Comment reject(Integer id) {
        currentUserService.requireAdmin();
        Comment comment = requireComment(id);
        if (comment.getStatus() == Comment.CommentStatus.approved) {
            comment.setStatus(Comment.CommentStatus.rejected);
            articleRepository.decrementCommentCount(comment.getArticle().getId());
        }
        return commentRepository.save(comment);
    }

    @Transactional
    public void delete(Integer id) {
        User actor = currentUserService.requireCurrentUser();
        Comment comment = requireComment(id);
        if (!comment.getUser().getId().equals(actor.getId()) && !currentUserService.isAdmin(actor)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "不能删除其他用户的评论");
        }
        if (comment.getStatus() == Comment.CommentStatus.approved) {
            articleRepository.decrementCommentCount(comment.getArticle().getId());
        }
        commentRepository.delete(comment);
    }

    @Transactional
    public Comment like(Integer id) {
        currentUserService.requireCurrentUser();
        requireComment(id);
        commentRepository.incrementLikeCount(id);
        return requireComment(id);
    }

    @Transactional
    public Comment unlike(Integer id) {
        currentUserService.requireCurrentUser();
        requireComment(id);
        commentRepository.decrementLikeCount(id);
        return requireComment(id);
    }

    private Comment requireComment(Integer id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "评论不存在"));
    }
}
