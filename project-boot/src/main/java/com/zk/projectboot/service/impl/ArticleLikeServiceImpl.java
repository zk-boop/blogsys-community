package com.zk.projectboot.service.impl;

import com.zk.projectboot.model.ArticleLike;
import com.zk.projectboot.repository.ArticleLikeRepository;
import com.zk.projectboot.service.ArticleLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ArticleLikeServiceImpl implements ArticleLikeService {

    private final ArticleLikeRepository articleLikeRepository;

    @Autowired
    public ArticleLikeServiceImpl(ArticleLikeRepository articleLikeRepository) {
        this.articleLikeRepository = articleLikeRepository;
    }

    @Override
    @Transactional
    public ArticleLike saveArticleLike(ArticleLike articleLike) {
        return articleLikeRepository.save(articleLike);
    }

    @Override
    public Optional<ArticleLike> getArticleLikeById(Integer id) {
        return articleLikeRepository.findById(id);
    }

    @Override
    public Optional<ArticleLike> getArticleLikeByArticleAndUser(Integer articleId, Integer userId) {
        return articleLikeRepository.findByArticleIdAndUserId(articleId, userId);
    }

    @Override
    public Page<ArticleLike> getArticleLikesByUserId(Integer userId, Pageable pageable) {
        return articleLikeRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public long countByArticleId(Integer articleId) {
        return articleLikeRepository.countByArticleId(articleId);
    }

    @Override
    public boolean existsByArticleIdAndUserId(Integer articleId, Integer userId) {
        return articleLikeRepository.existsByArticleIdAndUserId(articleId, userId);
    }

    @Override
    @Transactional
    public boolean deleteArticleLike(Integer id) {
        if (articleLikeRepository.existsById(id)) {
            articleLikeRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deleteByArticleIdAndUserId(Integer articleId, Integer userId) {
        if (existsByArticleIdAndUserId(articleId, userId)) {
            articleLikeRepository.deleteByArticleIdAndUserId(articleId, userId);
            return true;
        }
        return false;
    }
}
