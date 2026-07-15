package com.zk.projectboot.controller;

import com.zk.projectboot.application.ArticleApplicationService;
import com.zk.projectboot.dto.ApiResponse;
import com.zk.projectboot.dto.ArticleDTO;
import com.zk.projectboot.dto.ArticleWriteRequest;
import com.zk.projectboot.exception.BusinessException;
import com.zk.projectboot.model.Article;
import com.zk.projectboot.model.User;
import com.zk.projectboot.security.CurrentUserService;
import com.zk.projectboot.service.ArticleService;
import com.zk.projectboot.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;
    private final UserService userService;
    private final ArticleApplicationService articleApplicationService;
    private final CurrentUserService currentUserService;

    public ArticleController(ArticleService articleService,
                             UserService userService,
                             ArticleApplicationService articleApplicationService,
                             CurrentUserService currentUserService) {
        this.articleService = articleService;
        this.userService = userService;
        this.articleApplicationService = articleApplicationService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArticleDTO>> getArticleById(@PathVariable Integer id) {
        Article article = articleService.getArticleById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "文章不存在"));
        requireReadable(article);
        articleService.incrementViewCount(id);
        return ResponseEntity.ok(ApiResponse.success(ArticleDTO.fromArticle(article)));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<ArticleDTO>> getArticleBySlug(@PathVariable String slug) {
        Article article = articleService.getArticleBySlug(slug)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "文章不存在"));
        requireReadable(article);
        articleService.incrementViewCount(article.getId());
        return ResponseEntity.ok(ApiResponse.success(ArticleDTO.fromArticle(article)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ArticleDTO>>> getPublishedArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Article> articles = articleService.getPublishedArticles(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt")));
        return ResponseEntity.ok(ApiResponse.success(toDtos(articles)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ArticleDTO>>> searchArticles(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(toDtos(articleService.searchArticles(
                keyword, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"))))));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ArticleDTO>>> getByCategory(
            @PathVariable Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(toDtos(
                articleService.getPublishedArticlesByCategoryAndChildren(
                        categoryId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"))))));
    }

    @GetMapping("/tag/{tagId}")
    public ResponseEntity<ApiResponse<List<ArticleDTO>>> getByTag(
            @PathVariable Integer tagId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(toDtos(articleService.getArticlesByTag(
                tagId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"))))));
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<ApiResponse<List<ArticleDTO>>> getByAuthor(
            @PathVariable Integer authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User author = userService.getUserById(authorId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "作者不存在"));
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Optional<User> actor = currentUserService.findCurrentUser();
        boolean canReadDrafts = actor.isPresent()
                && (actor.get().getId().equals(authorId) || currentUserService.isAdmin(actor.get()));
        Page<Article> articles = canReadDrafts
                ? articleService.getArticlesByAuthor(author, pageable)
                : articleService.getPublishedArticlesByAuthor(author, pageable);
        return ResponseEntity.ok(ApiResponse.success(toDtos(articles)));
    }

    @GetMapping("/top")
    public ResponseEntity<ApiResponse<List<ArticleDTO>>> getTopArticles() {
        return ResponseEntity.ok(ApiResponse.success(articleService.getTopArticles().stream()
                .map(ArticleDTO::fromArticle).collect(Collectors.toList())));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<ArticleDTO>>> getFeaturedArticles() {
        return ResponseEntity.ok(ApiResponse.success(articleService.getFeaturedArticles().stream()
                .map(ArticleDTO::fromArticle).collect(Collectors.toList())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ArticleDTO>> createArticle(
            @Valid @RequestBody ArticleWriteRequest request,
            @RequestParam(required = false) Integer authorId,
            @RequestParam Integer categoryId) {
        return ResponseEntity.ok(ApiResponse.success(
                ArticleDTO.fromArticle(articleApplicationService.create(request, categoryId))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ArticleDTO>> updateArticle(
            @PathVariable Integer id,
            @Valid @RequestBody ArticleWriteRequest request,
            @RequestParam(required = false) Integer categoryId) {
        return ResponseEntity.ok(ApiResponse.success(
                ArticleDTO.fromArticle(articleApplicationService.update(id, request, categoryId))));
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<ArticleDTO>> publishArticle(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("文章已发布",
                ArticleDTO.fromArticle(articleApplicationService.publish(id))));
    }

    @PatchMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<ArticleDTO>> submitArticle(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("文章已提交审核",
                ArticleDTO.fromArticle(articleApplicationService.submit(id))));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<ArticleDTO>> rejectArticle(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("文章审核已拒绝",
                ArticleDTO.fromArticle(articleApplicationService.reject(id))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteArticle(@PathVariable Integer id) {
        articleApplicationService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("文章已删除", null));
    }

    private List<ArticleDTO> toDtos(Page<Article> articles) {
        return articles.getContent().stream().map(ArticleDTO::fromArticle).collect(Collectors.toList());
    }

    private void requireReadable(Article article) {
        if (article.getStatus() == Article.ArticleStatus.published) {
            return;
        }
        Optional<User> actor = currentUserService.findCurrentUser();
        if (!actor.isPresent()
                || (!actor.get().getId().equals(article.getAuthor().getId())
                && !currentUserService.isAdmin(actor.get()))) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "文章不存在");
        }
    }
}
