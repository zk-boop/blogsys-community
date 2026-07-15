package com.zk.projectboot.repository;

import com.zk.projectboot.model.ArticleFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.zk.projectboot.model.Article;

import java.util.Optional;

@Repository
public interface ArticleFavoriteRepository extends JpaRepository<ArticleFavorite, Integer> {

    Optional<ArticleFavorite> findByArticleIdAndUserId(Integer articleId, Integer userId);

    boolean existsByArticleIdAndUserId(Integer articleId, Integer userId);

    Page<ArticleFavorite> findByUserId(Integer userId, Pageable pageable);

    /**
     * 获取用户收藏的文章列表
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 分页的文章列表
     */
    @Query("SELECT af.article FROM ArticleFavorite af WHERE af.user.id = :userId")
    Page<Article> findArticlesByUserId(@Param("userId") Integer userId, Pageable pageable);

    long countByArticleId(Integer articleId);

    void deleteByArticleIdAndUserId(Integer articleId, Integer userId);
}
