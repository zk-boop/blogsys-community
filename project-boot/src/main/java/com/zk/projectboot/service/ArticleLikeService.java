package com.zk.projectboot.service;

import com.zk.projectboot.model.ArticleLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ArticleLikeService {

    ArticleLike saveArticleLike(ArticleLike articleLike);

    Optional<ArticleLike> getArticleLikeById(Integer id);

    Optional<ArticleLike> getArticleLikeByArticleAndUser(Integer articleId, Integer userId);

    Page<ArticleLike> getArticleLikesByUserId(Integer userId, Pageable pageable);

    long countByArticleId(Integer articleId);

    boolean existsByArticleIdAndUserId(Integer articleId, Integer userId);

    boolean deleteArticleLike(Integer id);

    boolean deleteByArticleIdAndUserId(Integer articleId, Integer userId);
}
