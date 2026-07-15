package com.zk.projectboot.repository;

import com.zk.projectboot.model.Article;
import com.zk.projectboot.model.Category;
import com.zk.projectboot.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 文章数据访问层接口
 * 提供文章实体的CRUD操作和自定义查询方法
 * 继承JpaRepository接口，自动获得基本的JPA操作方法
 */
@Repository  // 标记为Spring数据访问组件
public interface ArticleRepository extends JpaRepository<Article, Integer> {

    @Modifying
    @Query("UPDATE Article a SET a.commentCount = COALESCE(a.commentCount, 0) + 1 WHERE a.id = :id")
    int incrementCommentCount(@Param("id") Integer id);

    @Modifying
    @Query("UPDATE Article a SET a.commentCount = a.commentCount - 1 WHERE a.id = :id AND a.commentCount > 0")
    int decrementCommentCount(@Param("id") Integer id);

    @Modifying
    @Query("UPDATE Article a SET a.likeCount = COALESCE(a.likeCount, 0) + 1 WHERE a.id = :id")
    int incrementLikeCountAtomic(@Param("id") Integer id);

    @Modifying
    @Query("UPDATE Article a SET a.likeCount = a.likeCount - 1 WHERE a.id = :id AND a.likeCount > 0")
    int decrementLikeCountAtomic(@Param("id") Integer id);

    /**
     * 根据slug查找文章
     *
     * @param slug 文章的URL友好标识
     * @return 包含文章的Optional，如果不存在则为空
     */
    Optional<Article> findBySlug(String slug);

    /**
     * 检查指定slug的文章是否存在
     *
     * @param slug 文章的URL友好标识
     * @return 是否存在
     */
    boolean existsBySlug(String slug);

    /**
     * 根据文章状态分页查询文章列表
     *
     * @param status 文章状态（草稿、已发布、已归档）
     * @param pageable 分页和排序信息
     * @return 分页的文章列表
     */
    Page<Article> findByStatus(Article.ArticleStatus status, Pageable pageable);

    /**
     * 查询指定作者的所有文章
     *
     * @param author 作者用户对象
     * @param pageable 分页和排序信息
     * @return 分页的文章列表
     */
    Page<Article> findByAuthor(User author, Pageable pageable);

    /**
     * 查询指定分类的所有文章
     *
     * @param category 分类对象
     * @param pageable 分页和排序信息
     * @return 分页的文章列表
     */
    Page<Article> findByCategory(Category category, Pageable pageable);

    /**
     * 查询指定作者且指定状态的文章
     *
     * @param author 作者用户对象
     * @param status 文章状态
     * @param pageable 分页和排序信息
     * @return 分页的文章列表
     */
    Page<Article> findByAuthorAndStatus(User author, Article.ArticleStatus status, Pageable pageable);

    /**
     * 查询指定分类且指定状态的文章
     *
     * @param category 分类对象
     * @param status 文章状态
     * @param pageable 分页和排序信息
     * @return 分页的文章列表
     */
    Page<Article> findByCategoryAndStatus(Category category, Article.ArticleStatus status, Pageable pageable);

    /**
     * 根据关键词搜索文章
     * 在标题和内容中进行模糊匹配
     *
     * @param keyword 搜索关键词
     * @param status 文章状态过滤
     * @param pageable 分页和排序信息
     * @return 分页的搜索结果列表
     */
    @Query("SELECT a FROM Article a WHERE a.status = :status AND (a.title LIKE %:keyword% OR a.content LIKE %:keyword%)")
    Page<Article> searchArticles(@Param("keyword") String keyword, @Param("status") Article.ArticleStatus status, Pageable pageable);

    /**
     * 获取所有置顶且指定状态的文章
     * 按发布时间降序排序
     *
     * @param status 文章状态
     * @return 置顶文章列表
     */
    List<Article> findByIsTopTrueAndStatusOrderByPublishedAtDesc(Article.ArticleStatus status);

    /**
     * 获取所有特色且指定状态的文章
     * 按发布时间降序排序
     *
     * @param status 文章状态
     * @return 特色文章列表
     */
    List<Article> findByIsFeaturedTrueAndStatusOrderByPublishedAtDesc(Article.ArticleStatus status);

    /**
     * 查询包含指定标签且指定状态的文章
     * 使用原生SQL查询，通过文章-标签关联表进行连接
     *
     * @param tagId 标签ID
     * @param status 文章状态
     * @param pageable 分页和排序信息
     * @return 分页的文章列表
     */
    @Query(value = "SELECT a.* FROM articles a JOIN article_tags at ON a.id = at.article_id WHERE at.tag_id = :tagId AND a.status = :status ORDER BY a.created_at DESC",
           nativeQuery = true)
    Page<Article> findByTagIdAndStatus(@Param("tagId") Integer tagId, @Param("status") String status, Pageable pageable);

    /**
     * 查询指定状态且分类ID在指定列表中的文章
     *
     * @param status 文章状态
     * @param categoryIds 分类ID列表
     * @param pageable 分页和排序信息
     * @return 分页的文章列表
     */
    Page<Article> findByStatusAndCategoryIdIn(Article.ArticleStatus status, List<Integer> categoryIds, Pageable pageable);
}
