package com.zk.projectboot.service;

import com.zk.projectboot.model.ArticleFavorite;
import com.zk.projectboot.model.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ArticleFavoriteService {

    ArticleFavorite saveArticleFavorite(ArticleFavorite articleFavorite);

    Optional<ArticleFavorite> getArticleFavoriteById(Integer id);

    Optional<ArticleFavorite> getArticleFavoriteByArticleAndUser(Integer articleId, Integer userId);

    Page<ArticleFavorite> getArticleFavoritesByUserId(Integer userId, Pageable pageable);

    /**
     * 获取用户收藏的所有文章列表
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 用户收藏的文章分页结果
     */
    Page<Article> getArticlesByUserId(Integer userId, Pageable pageable);

    long countByArticleId(Integer articleId);

    boolean existsByArticleIdAndUserId(Integer articleId, Integer userId);

    boolean deleteArticleFavorite(Integer id);

    boolean deleteByArticleIdAndUserId(Integer articleId, Integer userId);
}
