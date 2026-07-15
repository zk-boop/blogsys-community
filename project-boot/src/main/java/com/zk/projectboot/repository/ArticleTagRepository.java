package com.zk.projectboot.repository;

import com.zk.projectboot.model.ArticleTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleTagRepository extends JpaRepository<ArticleTag, Integer> {

    List<ArticleTag> findByArticleId(Integer articleId);

    List<ArticleTag> findByTagId(Integer tagId);

    Optional<ArticleTag> findByArticleIdAndTagId(Integer articleId, Integer tagId);

    boolean existsByArticleIdAndTagId(Integer articleId, Integer tagId);

    void deleteByArticleId(Integer articleId);

    long countByTagId(Integer tagId);
}
