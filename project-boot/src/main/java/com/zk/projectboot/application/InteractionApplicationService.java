package com.zk.projectboot.application;

import com.zk.projectboot.exception.BusinessException;
import com.zk.projectboot.model.Article;
import com.zk.projectboot.model.ArticleFavorite;
import com.zk.projectboot.model.ArticleLike;
import com.zk.projectboot.model.User;
import com.zk.projectboot.repository.ArticleFavoriteRepository;
import com.zk.projectboot.repository.ArticleLikeRepository;
import com.zk.projectboot.repository.ArticleRepository;
import com.zk.projectboot.security.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InteractionApplicationService {

    private final ArticleRepository articleRepository;
    private final ArticleLikeRepository likeRepository;
    private final ArticleFavoriteRepository favoriteRepository;
    private final CurrentUserService currentUserService;

    public InteractionApplicationService(ArticleRepository articleRepository,
                                         ArticleLikeRepository likeRepository,
                                         ArticleFavoriteRepository favoriteRepository,
                                         CurrentUserService currentUserService) {
        this.articleRepository = articleRepository;
        this.likeRepository = likeRepository;
        this.favoriteRepository = favoriteRepository;
        this.currentUserService = currentUserService;
    }

    public boolean isLiked(Integer articleId) {
        User actor = currentUserService.requireCurrentUser();
        return likeRepository.existsByArticleIdAndUserId(articleId, actor.getId());
    }

    public boolean isFavorited(Integer articleId) {
        User actor = currentUserService.requireCurrentUser();
        return favoriteRepository.existsByArticleIdAndUserId(articleId, actor.getId());
    }

    @Transactional
    public boolean like(Integer articleId) {
        User actor = currentUserService.requireCurrentUser();
        Article article = requirePublishedArticle(articleId);
        if (likeRepository.existsByArticleIdAndUserId(articleId, actor.getId())) {
            return false;
        }
        ArticleLike like = new ArticleLike();
        like.setArticle(article);
        like.setUser(actor);
        likeRepository.save(like);
        articleRepository.incrementLikeCountAtomic(articleId);
        return true;
    }

    @Transactional
    public boolean unlike(Integer articleId) {
        User actor = currentUserService.requireCurrentUser();
        if (!likeRepository.existsByArticleIdAndUserId(articleId, actor.getId())) {
            return false;
        }
        likeRepository.deleteByArticleIdAndUserId(articleId, actor.getId());
        articleRepository.decrementLikeCountAtomic(articleId);
        return true;
    }

    @Transactional
    public boolean favorite(Integer articleId) {
        User actor = currentUserService.requireCurrentUser();
        Article article = requirePublishedArticle(articleId);
        if (favoriteRepository.existsByArticleIdAndUserId(articleId, actor.getId())) {
            return false;
        }
        ArticleFavorite favorite = new ArticleFavorite();
        favorite.setArticle(article);
        favorite.setUser(actor);
        favoriteRepository.save(favorite);
        return true;
    }

    @Transactional
    public boolean unfavorite(Integer articleId) {
        User actor = currentUserService.requireCurrentUser();
        if (!favoriteRepository.existsByArticleIdAndUserId(articleId, actor.getId())) {
            return false;
        }
        favoriteRepository.deleteByArticleIdAndUserId(articleId, actor.getId());
        return true;
    }

    private Article requirePublishedArticle(Integer articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "文章不存在"));
        if (article.getStatus() != Article.ArticleStatus.published) {
            throw new BusinessException(HttpStatus.CONFLICT, "只能操作已发布文章");
        }
        return article;
    }
}
