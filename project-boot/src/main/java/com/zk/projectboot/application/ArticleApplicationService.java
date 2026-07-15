package com.zk.projectboot.application;

import com.zk.projectboot.dto.ArticleWriteRequest;
import com.zk.projectboot.exception.BusinessException;
import com.zk.projectboot.model.Article;
import com.zk.projectboot.model.ArticleTag;
import com.zk.projectboot.model.Category;
import com.zk.projectboot.model.Tag;
import com.zk.projectboot.model.User;
import com.zk.projectboot.repository.ArticleRepository;
import com.zk.projectboot.repository.ArticleTagRepository;
import com.zk.projectboot.repository.CategoryRepository;
import com.zk.projectboot.repository.TagRepository;
import com.zk.projectboot.security.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class ArticleApplicationService {

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ArticleTagRepository articleTagRepository;
    private final CurrentUserService currentUserService;

    public ArticleApplicationService(ArticleRepository articleRepository,
                                     CategoryRepository categoryRepository,
                                     TagRepository tagRepository,
                                     ArticleTagRepository articleTagRepository,
                                     CurrentUserService currentUserService) {
        this.articleRepository = articleRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.articleTagRepository = articleTagRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public Article create(ArticleWriteRequest request, Integer categoryId) {
        User actor = currentUserService.requireCurrentUser();
        Category category = requireCategory(categoryId);

        Article article = new Article();
        apply(article, request, category);
        article.setAuthor(actor);
        article.setStatus(Article.ArticleStatus.draft);
        article.setSlug(uniqueSlug(request.getSlug(), request.getTitle(), null));
        Article saved = articleRepository.save(article);
        replaceTags(saved, request.getTagIds());
        return saved;
    }

    @Transactional
    public Article update(Integer articleId, ArticleWriteRequest request, Integer categoryId) {
        User actor = currentUserService.requireCurrentUser();
        Article article = requireArticle(articleId);
        requireOwnerOrAdmin(article, actor);
        if (article.getStatus() == Article.ArticleStatus.published && !currentUserService.isAdmin(actor)) {
            throw new BusinessException(HttpStatus.CONFLICT, "已发布文章不能由普通用户直接修改");
        }

        Category category = categoryId == null ? article.getCategory() : requireCategory(categoryId);
        apply(article, request, category);
        article.setSlug(uniqueSlug(request.getSlug(), request.getTitle(), article.getId()));
        Article saved = articleRepository.save(article);
        replaceTags(saved, request.getTagIds());
        return saved;
    }

    @Transactional
    public Article submit(Integer articleId) {
        User actor = currentUserService.requireCurrentUser();
        Article article = requireArticle(articleId);
        requireOwnerOrAdmin(article, actor);
        if (article.getStatus() != Article.ArticleStatus.draft) {
            throw new BusinessException(HttpStatus.CONFLICT, "只有草稿可以提交审核");
        }
        article.setStatus(Article.ArticleStatus.pending);
        return articleRepository.save(article);
    }

    @Transactional
    public Article publish(Integer articleId) {
        currentUserService.requireAdmin();
        Article article = requireArticle(articleId);
        if (article.getStatus() != Article.ArticleStatus.pending
                && article.getStatus() != Article.ArticleStatus.draft) {
            throw new BusinessException(HttpStatus.CONFLICT, "当前状态不能发布");
        }
        Category category = article.getCategory();
        if (category != null && !"1".equals(category.getStatus())) {
            article.setCategory(requireCategory(8));
        }
        article.setStatus(Article.ArticleStatus.published);
        article.setPublishedAt(LocalDateTime.now());
        return articleRepository.save(article);
    }

    @Transactional
    public Article reject(Integer articleId) {
        currentUserService.requireAdmin();
        Article article = requireArticle(articleId);
        if (article.getStatus() != Article.ArticleStatus.pending) {
            throw new BusinessException(HttpStatus.CONFLICT, "只有待审核文章可以拒绝");
        }
        article.setStatus(Article.ArticleStatus.draft);
        return articleRepository.save(article);
    }

    @Transactional
    public void delete(Integer articleId) {
        User actor = currentUserService.requireCurrentUser();
        Article article = requireArticle(articleId);
        requireOwnerOrAdmin(article, actor);
        articleRepository.delete(article);
    }

    private Article requireArticle(Integer id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "文章不存在"));
    }

    private Category requireCategory(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "分类不存在"));
    }

    private void requireOwnerOrAdmin(Article article, User actor) {
        if (!article.getAuthor().getId().equals(actor.getId()) && !currentUserService.isAdmin(actor)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "不能操作其他用户的文章");
        }
    }

    private void apply(Article article, ArticleWriteRequest request, Category category) {
        article.setTitle(request.getTitle().trim());
        article.setContent(request.getContent());
        article.setExcerpt(request.getExcerpt());
        article.setCoverImage(request.getCoverImage());
        article.setCategory(category);
    }

    private String uniqueSlug(String requestedSlug, String title, Integer currentArticleId) {
        String source = requestedSlug == null || requestedSlug.trim().isEmpty() ? title : requestedSlug;
        String slug = source.toLowerCase().trim()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9-]", "")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        if (slug.isEmpty()) {
            slug = "article-" + UUID.randomUUID().toString().substring(0, 8);
        }
        final String candidate = slug;
        boolean occupied = articleRepository.findBySlug(candidate)
                .map(article -> !article.getId().equals(currentArticleId))
                .orElse(false);
        return occupied ? candidate + "-" + UUID.randomUUID().toString().substring(0, 8) : candidate;
    }

    private void replaceTags(Article article, List<Integer> tagIds) {
        articleTagRepository.deleteByArticleId(article.getId());
        List<Integer> ids = tagIds == null ? Collections.emptyList() : tagIds;
        for (Integer tagId : ids) {
            Tag tag = tagRepository.findById(tagId)
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "标签不存在: " + tagId));
            ArticleTag articleTag = new ArticleTag();
            articleTag.setArticle(article);
            articleTag.setTag(tag);
            articleTagRepository.save(articleTag);
        }
    }
}
