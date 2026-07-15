package com.zk.projectboot.service.impl;

import com.zk.projectboot.model.ArticleFavorite;
import com.zk.projectboot.repository.ArticleFavoriteRepository;
import com.zk.projectboot.service.ArticleFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import com.zk.projectboot.model.Article;

@Service
public class ArticleFavoriteServiceImpl implements ArticleFavoriteService {

    private final ArticleFavoriteRepository articleFavoriteRepository;

    @Autowired
    public ArticleFavoriteServiceImpl(ArticleFavoriteRepository articleFavoriteRepository) {
        this.articleFavoriteRepository = articleFavoriteRepository;
    }

    @Override
    @Transactional
    public ArticleFavorite saveArticleFavorite(ArticleFavorite articleFavorite) {
        return articleFavoriteRepository.save(articleFavorite);
    }

    @Override
    public Optional<ArticleFavorite> getArticleFavoriteById(Integer id) {
        return articleFavoriteRepository.findById(id);
    }

    @Override
    public Optional<ArticleFavorite> getArticleFavoriteByArticleAndUser(Integer articleId, Integer userId) {
        return articleFavoriteRepository.findByArticleIdAndUserId(articleId, userId);
    }

    @Override
    public Page<ArticleFavorite> getArticleFavoritesByUserId(Integer userId, Pageable pageable) {
        return articleFavoriteRepository.findByUserId(userId, pageable);
    }

    /**
     * 获取用户收藏的所有文章列表
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 用户收藏的文章分页结果
     */
    @Override
    public Page<Article> getArticlesByUserId(Integer userId, Pageable pageable) {
        return articleFavoriteRepository.findArticlesByUserId(userId, pageable);
    }

    @Override
    public long countByArticleId(Integer articleId) {
        return articleFavoriteRepository.countByArticleId(articleId);
    }

    @Override
    public boolean existsByArticleIdAndUserId(Integer articleId, Integer userId) {
        return articleFavoriteRepository.existsByArticleIdAndUserId(articleId, userId);
    }

    @Override
    @Transactional
    public boolean deleteArticleFavorite(Integer id) {
        if (articleFavoriteRepository.existsById(id)) {
            articleFavoriteRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deleteByArticleIdAndUserId(Integer articleId, Integer userId) {
        if (existsByArticleIdAndUserId(articleId, userId)) {
            articleFavoriteRepository.deleteByArticleIdAndUserId(articleId, userId);
            return true;
        }
        return false;
    }
}
