package com.zk.projectboot.service;

import com.zk.projectboot.model.Article;
import com.zk.projectboot.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 文章服务接口
 * 定义了与文章相关的所有业务操作
 * 包括文章的CRUD、查询、搜索等功能
 */
public interface ArticleService {

    /**
     * 保存新文章
     * @param article 要保存的文章对象
     * @return 保存后的文章对象（包含生成的ID等信息）
     */
    Article saveArticle(Article article);

    /**
     * 通过ID获取文章
     * @param id 文章ID
     * @return 包含文章对象的Optional，如果不存在则为空
     */
    Optional<Article> getArticleById(Integer id);

    /**
     * 通过slug（URL友好标识）获取文章
     * @param slug 文章slug
     * @return 包含文章对象的Optional，如果不存在则为空
     */
    Optional<Article> getArticleBySlug(String slug);

    /**
     * 获取所有文章，支持分页
     * @param pageable 分页和排序信息
     * @return 分页后的文章列表
     */
    Page<Article> getAllArticles(Pageable pageable);

    /**
     * 获取所有已发布的文章，支持分页
     * @param pageable 分页和排序信息
     * @return 分页后的已发布文章列表
     */
    Page<Article> getPublishedArticles(Pageable pageable);

    /**
     * 获取指定状态的文章，支持分页
     * @param status 文章状态
     * @param pageable 分页和排序信息
     * @return 分页后的指定状态文章列表
     */
    Page<Article> getArticlesByStatus(Article.ArticleStatus status, Pageable pageable);

    /**
     * 获取指定作者的所有文章，支持分页
     * @param author 作者对象
     * @param pageable 分页和排序信息
     * @return 分页后的作者文章列表
     */
    Page<Article> getArticlesByAuthor(User author, Pageable pageable);

    /**
     * 获取指定作者的所有已发布文章，支持分页
     * @param author 作者对象
     * @param pageable 分页和排序信息
     * @return 分页后的作者已发布文章列表
     */
    Page<Article> getPublishedArticlesByAuthor(User author, Pageable pageable);

    /**
     * 获取指定分类的所有文章，支持分页
     * @param categoryId 分类ID
     * @param pageable 分页和排序信息
     * @return 分页后的分类文章列表
     */
    Page<Article> getArticlesByCategory(Integer categoryId, Pageable pageable);

    /**
     * 获取指定分类的所有已发布文章，支持分页
     * @param categoryId 分类ID
     * @param pageable 分页和排序信息
     * @return 分页后的分类已发布文章列表
     */
    Page<Article> getPublishedArticlesByCategory(Integer categoryId, Pageable pageable);

    /**
     * 获取包含指定标签的所有文章，支持分页
     * @param tagId 标签ID
     * @param pageable 分页和排序信息
     * @return 分页后的标签文章列表
     */
    Page<Article> getArticlesByTag(Integer tagId, Pageable pageable);

    /**
     * 根据关键词搜索文章，支持分页
     * @param keyword 搜索关键词
     * @param pageable 分页和排序信息
     * @return 分页后的搜索结果列表
     */
    Page<Article> searchArticles(String keyword, Pageable pageable);

    /**
     * 获取所有置顶文章
     * @return 置顶文章列表
     */
    List<Article> getTopArticles();

    /**
     * 获取所有特色文章
     * @return 特色文章列表
     */
    List<Article> getFeaturedArticles();

    /**
     * 更新文章信息
     * @param article 更新后的文章对象
     * @return 更新后保存的文章对象
     */
    Article updateArticle(Article article);

    /**
     * 发布文章
     * 将文章状态改为已发布，并设置发布时间
     * @param id 文章ID
     * @return 操作是否成功
     */
    boolean publishArticle(Integer id);

    /**
     * 提交文章审核
     * 将文章状态改为待审核
     * @param id 文章ID
     * @return 操作是否成功
     */
    boolean submitForReview(Integer id);

    /**
     * 拒绝文章审核
     * 将文章状态改回草稿
     * @param id 文章ID
     * @return 操作是否成功
     */
    boolean rejectArticle(Integer id);

    /**
     * 删除文章
     * @param id 文章ID
     * @return 操作是否成功
     */
    boolean deleteArticle(Integer id);

    /**
     * 增加文章浏览次数
     * @param id 文章ID
     * @return 操作是否成功
     */
    boolean incrementViewCount(Integer id);

    /**
     * 增加文章点赞次数
     * @param id 文章ID
     * @return 操作是否成功
     */
    boolean incrementLikeCount(Integer id);

    /**
     * 减少文章点赞次数
     * @param id 文章ID
     * @return 操作是否成功
     */
    boolean decrementLikeCount(Integer id);

    /**
     * 检查指定slug的文章是否存在
     * @param slug 文章slug
     * @return 是否存在
     */
    boolean existsBySlug(String slug);

    /**
     * 获取指定分类及其所有子分类的已发布文章，支持分页
     * @param categoryId 分类ID
     * @param pageable 分页和排序信息
     * @return 分页后的分类及其子分类的已发布文章列表
     */
    Page<Article> getPublishedArticlesByCategoryAndChildren(Integer categoryId, Pageable pageable);

    /**
     * 设置文章推荐状态
     * @param id 文章ID
     * @param featured 是否推荐
     * @return 操作是否成功
     */
    boolean setArticleFeatured(Integer id, boolean featured);

    /**
     * 设置文章置顶状态
     * @param id 文章ID
     * @param top 是否置顶
     * @return 操作是否成功
     */
    boolean setArticleTop(Integer id, boolean top);
}
