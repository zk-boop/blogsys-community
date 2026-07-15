package com.zk.projectboot.repository;

import com.zk.projectboot.model.Article;
import com.zk.projectboot.model.Comment;
import com.zk.projectboot.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = COALESCE(c.likeCount, 0) + 1 WHERE c.id = :id")
    int incrementLikeCount(@Param("id") Integer id);

    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount - 1 WHERE c.id = :id AND c.likeCount > 0")
    int decrementLikeCount(@Param("id") Integer id);

    Page<Comment> findByArticleAndParentIsNullAndStatus(Article article, Comment.CommentStatus status, Pageable pageable);

    Page<Comment> findByUser(User user, Pageable pageable);

    Page<Comment> findByStatus(Comment.CommentStatus status, Pageable pageable);

    List<Comment> findByParentIdAndStatus(Integer parentId, Comment.CommentStatus status);

    long countByArticleAndStatus(Article article, Comment.CommentStatus status);
}
