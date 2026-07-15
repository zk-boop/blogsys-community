package com.zk.projectboot.service.impl;

import com.zk.projectboot.model.Article;
import com.zk.projectboot.model.Category;
import com.zk.projectboot.model.User;
import com.zk.projectboot.repository.ArticleRepository;
import com.zk.projectboot.repository.CategoryRepository;
import com.zk.projectboot.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

/**
 * 文章服务接口实现类
 * 实现ArticleService接口中定义的所有方法
 * 提供文章的增删改查、搜索、状态管理等业务逻辑
 */
@Service  // 标记为Spring服务组件
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 构造函数注入依赖的仓库组件
     * @param articleRepository 文章仓库
     * @param categoryRepository 分类仓库
     */
    @Autowired
    public ArticleServiceImpl(ArticleRepository articleRepository, CategoryRepository categoryRepository) {
        this.articleRepository = articleRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * 保存新文章
     * 使用@Transactional确保操作的原子性
     * @param article 文章对象
     * @return 保存后的文章对象
     */
    @Override
    @Transactional
    public Article saveArticle(Article article) {
        return articleRepository.save(article);
    }

    /**
     * 通过ID获取文章
     * @param id 文章ID
     * @return 包含文章对象的Optional，如果不存在则为空
     */
    @Override
    public Optional<Article> getArticleById(Integer id) {
        return articleRepository.findById(id);
    }

    /**
     * 通过slug获取文章
     * @param slug 文章slug（URL友好标识）
     * @return 包含文章对象的Optional，如果不存在则为空
     */
    @Override
    public Optional<Article> getArticleBySlug(String slug) {
        return articleRepository.findBySlug(slug);
    }

    /**
     * 获取所有文章，支持分页和排序
     * @param pageable 分页和排序信息
     * @return 分页后的文章列表
     */
    @Override
    public Page<Article> getAllArticles(Pageable pageable) {
        return articleRepository.findAll(pageable);
    }

    /**
     * 获取所有已发布文章，支持分页和排序
     * @param pageable 分页和排序信息
     * @return 分页后的已发布文章列表
     */
    @Override
    public Page<Article> getPublishedArticles(Pageable pageable) {
        return articleRepository.findByStatus(Article.ArticleStatus.published, pageable);
    }

    /**
     * 获取指定状态的文章，支持分页和排序
     * @param status 文章状态
     * @param pageable 分页和排序信息
     * @return 分页后的指定状态文章列表
     */
    @Override
    public Page<Article> getArticlesByStatus(Article.ArticleStatus status, Pageable pageable) {
        return articleRepository.findByStatus(status, pageable);
    }

    /**
     * 获取指定作者的所有文章，支持分页和排序
     * @param author 作者对象
     * @param pageable 分页和排序信息
     * @return 分页后的作者文章列表
     */
    @Override
    public Page<Article> getArticlesByAuthor(User author, Pageable pageable) {
        return articleRepository.findByAuthor(author, pageable);
    }

    /**
     * 获取指定作者的所有已发布文章，支持分页和排序
     * @param author 作者对象
     * @param pageable 分页和排序信息
     * @return 分页后的作者已发布文章列表
     */
    @Override
    public Page<Article> getPublishedArticlesByAuthor(User author, Pageable pageable) {
        return articleRepository.findByAuthorAndStatus(author, Article.ArticleStatus.published, pageable);
    }

    /**
     * 获取指定分类的所有文章，支持分页和排序
     * 首先检查分类是否存在，不存在则返回空页
     * @param categoryId 分类ID
     * @param pageable 分页和排序信息
     * @return 分页后的分类文章列表
     */
    @Override
    public Page<Article> getArticlesByCategory(Integer categoryId, Pageable pageable) {
        Optional<Category> categoryOptional = categoryRepository.findById(categoryId);
        if (categoryOptional.isPresent()) {
            return articleRepository.findByCategory(categoryOptional.get(), pageable);
        }
        return Page.empty(pageable);
    }

    /**
     * 获取指定分类的所有已发布文章，支持分页和排序
     * 首先检查分类是否存在，不存在则返回空页
     * @param categoryId 分类ID
     * @param pageable 分页和排序信息
     * @return 分页后的分类已发布文章列表
     */
    @Override
    public Page<Article> getPublishedArticlesByCategory(Integer categoryId, Pageable pageable) {
        Optional<Category> categoryOptional = categoryRepository.findById(categoryId);
        if (categoryOptional.isPresent()) {
            return articleRepository.findByCategoryAndStatus(categoryOptional.get(), Article.ArticleStatus.published, pageable);
        }
        return Page.empty(pageable);
    }

    /**
     * 获取包含指定标签的所有已发布文章，支持分页和排序
     * @param tagId 标签ID
     * @param pageable 分页和排序信息
     * @return 分页后的标签文章列表
     */
    @Override
    public Page<Article> getArticlesByTag(Integer tagId, Pageable pageable) {
        return articleRepository.findByTagIdAndStatus(tagId, Article.ArticleStatus.published, pageable);
    }

    /**
     * 根据关键词搜索已发布的文章，支持分页和排序
     * 具体搜索逻辑在Repository中实现
     * @param keyword 搜索关键词
     * @param pageable 分页和排序信息
     * @return 分页后的搜索结果列表
     */
    @Override
    public Page<Article> searchArticles(String keyword, Pageable pageable) {
        return articleRepository.searchArticles(keyword, Article.ArticleStatus.published, pageable);
    }

    /**
     * 获取所有已发布的置顶文章
     * @return 置顶文章列表，按发布时间降序排序
     */
    @Override
    public List<Article> getTopArticles() {
        return articleRepository.findByIsTopTrueAndStatusOrderByPublishedAtDesc(Article.ArticleStatus.published);
    }

    /**
     * 获取所有已发布的特色文章
     * @return 特色文章列表，按发布时间降序排序
     */
    @Override
    public List<Article> getFeaturedArticles() {
        return articleRepository.findByIsFeaturedTrueAndStatusOrderByPublishedAtDesc(Article.ArticleStatus.published);
    }

    /**
     * 更新文章信息
     * 使用@Transactional确保操作的原子性
     * @param article 更新后的文章对象
     * @return 更新后保存的文章对象
     */
    @Override
    @Transactional
    public Article updateArticle(Article article) {
        return articleRepository.save(article);
    }

    /**
     * 发布文章
     * 将文章状态改为已发布，并设置发布时间
     * 使用@Transactional确保操作的原子性
     * @param id 文章ID
     * @return 操作是否成功
     */
    @Override
    @Transactional
    public boolean publishArticle(Integer id) {
        Optional<Article> articleOptional = articleRepository.findById(id);
        if (articleOptional.isPresent()) {
            Article article = articleOptional.get();

            // 检查分类状态，如果不是已批准状态("1")，则使用"其他"分类(ID=8)
            Category category = article.getCategory();
            if (category != null && !"1".equals(category.getStatus())) {
                Optional<Category> otherCategoryOptional = categoryRepository.findById(8);
                if (otherCategoryOptional.isPresent()) {
                    article.setCategory(otherCategoryOptional.get());
                }
            }

            article.setStatus(Article.ArticleStatus.published);
            article.setPublishedAt(LocalDateTime.now());
            articleRepository.save(article);
            return true;
        }
        return false;
    }

    /**
     * 提交文章审核
     * 将文章状态改为待审核
     * 使用@Transactional确保操作的原子性
     * @param id 文章ID
     * @return 操作是否成功
     */
    @Override
    @Transactional
    public boolean submitForReview(Integer id) {
        Optional<Article> articleOptional = articleRepository.findById(id);
        if (articleOptional.isPresent()) {
            Article article = articleOptional.get();
            article.setStatus(Article.ArticleStatus.pending);
            articleRepository.save(article);
            return true;
        }
        return false;
    }

    /**
     * 拒绝文章审核
     * 将文章状态改回草稿
     * 使用@Transactional确保操作的原子性
     * @param id 文章ID
     * @return 操作是否成功
     */
    @Override
    @Transactional
    public boolean rejectArticle(Integer id) {
        Optional<Article> articleOptional = articleRepository.findById(id);
        if (articleOptional.isPresent()) {
            Article article = articleOptional.get();
            article.setStatus(Article.ArticleStatus.draft);
            articleRepository.save(article);
            return true;
        }
        return false;
    }

    /**
     * 删除文章
     * 使用@Transactional确保操作的原子性
     * @param id 文章ID
     * @return 操作是否成功
     */
    @Override
    @Transactional
    public boolean deleteArticle(Integer id) {
        if (articleRepository.existsById(id)) {
            articleRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * 增加文章浏览次数
     * 使用@Transactional确保操作的原子性
     * @param id 文章ID
     * @return 操作是否成功
     */
    @Override
    @Transactional
    public boolean incrementViewCount(Integer id) {
        Optional<Article> articleOptional = articleRepository.findById(id);
        if (articleOptional.isPresent()) {
            Article article = articleOptional.get();
            article.setViewCount(article.getViewCount() + 1);
            articleRepository.save(article);
            return true;
        }
        return false;
    }

    /**
     * 检查指定slug的文章是否存在
     * @param slug 文章slug
     * @return 是否存在
     */
    @Override
    public boolean existsBySlug(String slug) {
        return articleRepository.existsBySlug(slug);
    }

    /**
     * 获取指定分类及其所有子分类的已发布文章，支持分页
     * 首先获取指定分类及所有子分类，然后查找这些分类下的所有已发布文章
     * @param categoryId 分类ID
     * @param pageable 分页和排序信息
     * @return 分页后的分类及其子分类的已发布文章列表
     */
    @Override
    public Page<Article> getPublishedArticlesByCategoryAndChildren(Integer categoryId, Pageable pageable) {
        Optional<Category> categoryOptional = categoryRepository.findById(categoryId);
        if (!categoryOptional.isPresent()) {
            return Page.empty(pageable);
        }

        Category parentCategory = categoryOptional.get();
        List<Integer> categoryIds = new ArrayList<>();
        categoryIds.add(parentCategory.getId());

        // 获取所有子分类
        List<Category> subCategories = categoryRepository.findByParentId(categoryId);
        for (Category subCategory : subCategories) {
            categoryIds.add(subCategory.getId());
        }

        return articleRepository.findByStatusAndCategoryIdIn(Article.ArticleStatus.published, categoryIds, pageable);
    }

    /**
     * 设置文章推荐状态
     * @param id 文章ID
     * @param featured 是否推荐
     * @return 操作是否成功
     */
    @Override
    @Transactional
    public boolean setArticleFeatured(Integer id, boolean featured) {
        Optional<Article> articleOptional = articleRepository.findById(id);
        if (articleOptional.isPresent()) {
            Article article = articleOptional.get();
            article.setIsFeatured(featured);
            articleRepository.save(article);
            return true;
        }
        return false;
    }

    /**
     * 设置文章置顶状态
     * @param id 文章ID
     * @param top 是否置顶
     * @return 操作是否成功
     */
    @Override
    @Transactional
    public boolean setArticleTop(Integer id, boolean top) {
        Optional<Article> articleOptional = articleRepository.findById(id);
        if (articleOptional.isPresent()) {
            Article article = articleOptional.get();
            article.setIsTop(top);
            articleRepository.save(article);
            return true;
        }
        return false;
    }

    /**
     * 增加文章点赞次数
     * 使用@Transactional确保操作的原子性
     * @param id 文章ID
     * @return 操作是否成功
     */
    @Override
    @Transactional
    public boolean incrementLikeCount(Integer id) {
        Optional<Article> articleOptional = articleRepository.findById(id);
        if (articleOptional.isPresent()) {
            Article article = articleOptional.get();
            article.setLikeCount(article.getLikeCount() + 1);
            articleRepository.save(article);
            return true;
        }
        return false;
    }

    /**
     * 减少文章点赞次数
     * 使用@Transactional确保操作的原子性
     * @param id 文章ID
     * @return 操作是否成功
     */
    @Override
    @Transactional
    public boolean decrementLikeCount(Integer id) {
        Optional<Article> articleOptional = articleRepository.findById(id);
        if (articleOptional.isPresent()) {
            Article article = articleOptional.get();
            int likeCount = Math.max(0, article.getLikeCount() - 1);
            article.setLikeCount(likeCount);
            articleRepository.save(article);
            return true;
        }
        return false;
    }
}
