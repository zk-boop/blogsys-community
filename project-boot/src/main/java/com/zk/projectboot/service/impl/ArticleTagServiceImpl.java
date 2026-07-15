package com.zk.projectboot.service.impl;

import com.zk.projectboot.model.ArticleTag;
import com.zk.projectboot.repository.ArticleTagRepository;
import com.zk.projectboot.service.ArticleTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ArticleTagServiceImpl implements ArticleTagService {

    private final ArticleTagRepository articleTagRepository;

    @Autowired
    public ArticleTagServiceImpl(ArticleTagRepository articleTagRepository) {
        this.articleTagRepository = articleTagRepository;
    }

    @Override
    @Transactional
    public ArticleTag saveArticleTag(ArticleTag articleTag) {
        return articleTagRepository.save(articleTag);
    }

    @Override
    public Optional<ArticleTag> getArticleTagById(Integer id) {
        return articleTagRepository.findById(id);
    }

    @Override
    public Optional<ArticleTag> getArticleTagByArticleAndTag(Integer articleId, Integer tagId) {
        return articleTagRepository.findByArticleIdAndTagId(articleId, tagId);
    }

    @Override
    public List<ArticleTag> getArticleTagsByArticleId(Integer articleId) {
        return articleTagRepository.findByArticleId(articleId);
    }

    @Override
    public List<ArticleTag> getArticleTagsByTagId(Integer tagId) {
        return articleTagRepository.findByTagId(tagId);
    }

    @Override
    public boolean existsByArticleIdAndTagId(Integer articleId, Integer tagId) {
        return articleTagRepository.existsByArticleIdAndTagId(articleId, tagId);
    }

    @Override
    @Transactional
    public void deleteArticleTagsByArticleId(Integer articleId) {
        articleTagRepository.deleteByArticleId(articleId);
    }

    @Override
    public long countByTagId(Integer tagId) {
        return articleTagRepository.countByTagId(tagId);
    }

    @Override
    @Transactional
    public boolean deleteArticleTag(Integer id) {
        if (articleTagRepository.existsById(id)) {
            articleTagRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
