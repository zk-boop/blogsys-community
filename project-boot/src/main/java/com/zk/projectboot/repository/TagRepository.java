package com.zk.projectboot.repository;

import com.zk.projectboot.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {
    Optional<Tag> findByName(String name);

    Page<Tag> findByNameContaining(String keyword, Pageable pageable);

    @Query(value = "SELECT t.* FROM tags t JOIN article_tags at ON t.id = at.tag_id " +
           "GROUP BY t.id ORDER BY COUNT(at.id) DESC", nativeQuery = true)
    List<Tag> findHotTags(Pageable pageable);

    @Query(value = "SELECT t.* FROM tags t JOIN article_tags at ON t.id = at.tag_id " +
           "WHERE at.article_id = :articleId", nativeQuery = true)
    List<Tag> findByArticleId(@Param("articleId") Integer articleId);

    boolean existsByName(String name);
}
