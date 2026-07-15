package com.zk.projectboot.service;

import com.zk.projectboot.model.ArticleTag;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ArticleTagService {

    ArticleTag saveArticleTag(ArticleTag articleTag);

    Optional<ArticleTag> getArticleTagById(Integer id);

    Optional<ArticleTag> getArticleTagByArticleAndTag(Integer articleId, Integer tagId);

    List<ArticleTag> getArticleTagsByArticleId(Integer articleId);

    List<ArticleTag> getArticleTagsByTagId(Integer tagId);

    boolean existsByArticleIdAndTagId(Integer articleId, Integer tagId);

    @Transactional
    void deleteArticleTagsByArticleId(Integer articleId);

    long countByTagId(Integer tagId);

    boolean deleteArticleTag(Integer id);
}
