package com.zk.projectboot.controller;

import com.zk.projectboot.application.InteractionApplicationService;
import com.zk.projectboot.dto.ApiResponse;
import com.zk.projectboot.dto.ArticleDTO;
import com.zk.projectboot.exception.BusinessException;
import com.zk.projectboot.model.Article;
import com.zk.projectboot.model.User;
import com.zk.projectboot.security.CurrentUserService;
import com.zk.projectboot.service.ArticleFavoriteService;
import com.zk.projectboot.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorites")
public class ArticleFavoriteController {

    private final InteractionApplicationService interactionService;
    private final ArticleFavoriteService favoriteService;
    private final UserService userService;
    private final CurrentUserService currentUserService;

    public ArticleFavoriteController(InteractionApplicationService interactionService,
                                     ArticleFavoriteService favoriteService,
                                     UserService userService,
                                     CurrentUserService currentUserService) {
        this.interactionService = interactionService;
        this.favoriteService = favoriteService;
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> check(
            @RequestParam Integer articleId,
            @RequestParam(required = false) Integer userId) {
        return ResponseEntity.ok(ApiResponse.success(interactionService.isFavorited(articleId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> favorite(@RequestParam Integer articleId) {
        boolean created = interactionService.favorite(articleId);
        return ResponseEntity.ok(created
                ? ApiResponse.success("收藏成功")
                : ApiResponse.success("已收藏，无需重复操作"));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<String>> unfavorite(@RequestParam Integer articleId) {
        boolean removed = interactionService.unfavorite(articleId);
        return ResponseEntity.ok(removed
                ? ApiResponse.success("取消收藏成功")
                : ApiResponse.success("未收藏，无需取消"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getUserFavorites(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        currentUserService.requireSelfOrAdmin(userId);
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "用户不存在"));
        Page<Article> articles = favoriteService.getArticlesByUserId(user.getId(), PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(articles.map(ArticleDTO::fromArticle)));
    }
}
