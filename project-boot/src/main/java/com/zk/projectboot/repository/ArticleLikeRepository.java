package com.zk.projectboot.repository;

import com.zk.projectboot.model.ArticleLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Integer> {
    Optional<ArticleLike> findByArticleIdAndUserId(Integer articleId, Integer userId);

    List<ArticleLike> findByArticleId(Integer articleId);

    List<ArticleLike> findByUserId(Integer userId);

    Page<ArticleLike> findByUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);

    boolean existsByArticleIdAndUserId(Integer articleId, Integer userId);

    void deleteByArticleIdAndUserId(Integer articleId, Integer userId);

    long countByArticleId(Integer articleId);
}
