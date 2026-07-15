package com.zk.projectboot.controller;

import com.zk.projectboot.dto.ApiResponse;
import com.zk.projectboot.dto.ArticleDTO;
import com.zk.projectboot.dto.CategoryDTO;
import com.zk.projectboot.dto.TagDTO;
import com.zk.projectboot.dto.UserDTO;
import com.zk.projectboot.model.Category;
import com.zk.projectboot.model.Tag;
import com.zk.projectboot.model.User;
import com.zk.projectboot.service.ArticleService;
import com.zk.projectboot.service.CategoryService;
import com.zk.projectboot.service.TagService;
import com.zk.projectboot.service.UserService;
import com.zk.projectboot.application.ArticleApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final ArticleService articleService;
    private final TagService tagService;
    private final CategoryService categoryService;
    private final ArticleApplicationService articleApplicationService;

    @Autowired
    public AdminController(UserService userService, ArticleService articleService,
                          TagService tagService, CategoryService categoryService,
                          ArticleApplicationService articleApplicationService) {
        this.userService = userService;
        this.articleService = articleService;
        this.tagService = tagService;
        this.categoryService = categoryService;
        this.articleApplicationService = articleApplicationService;
    }

    // 用户管理

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<User> userPage = userService.getAllUsers(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        List<UserDTO> userDTOs = userPage.getContent().stream()
                .map(UserDTO::fromUser)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(userDTOs));
    }

    @GetMapping("/users/{status}")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getUsersByStatus(
            @PathVariable User.UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<User> userPage = userService.getUsersByStatus(status,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        List<UserDTO> userDTOs = userPage.getContent().stream()
                .map(UserDTO::fromUser)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(userDTOs));
    }

    @PatchMapping("/users/{id}/ban")
    public ResponseEntity<ApiResponse<UserDTO>> banUser(@PathVariable Integer id) {
        if (userService.banUser(id)) {
            Optional<User> userOptional = userService.getUserById(id);
            return ResponseEntity.ok(ApiResponse.success("用户已封禁",
                    UserDTO.fromUser(userOptional.get())));
        }
        return ResponseEntity.ok(ApiResponse.fail("用户不存在或封禁失败"));
    }

    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<ApiResponse<UserDTO>> activateUser(@PathVariable Integer id) {
        if (userService.activateUser(id)) {
            Optional<User> userOptional = userService.getUserById(id);
            return ResponseEntity.ok(ApiResponse.success("用户已激活",
                    UserDTO.fromUser(userOptional.get())));
        }
        return ResponseEntity.ok(ApiResponse.fail("用户不存在或激活失败"));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("用户已删除", null));
    }

    // 文章管理

    @GetMapping("/articles")
    public ResponseEntity<ApiResponse<List<ArticleDTO>>> getAllArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<com.zk.projectboot.model.Article> articlePage = articleService.getAllArticles(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        List<ArticleDTO> articleDTOs = articlePage.getContent().stream()
                .map(ArticleDTO::fromArticle)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(articleDTOs));
    }

    @GetMapping("/articles/pending")
    public ResponseEntity<ApiResponse<List<ArticleDTO>>> getPendingArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // 查询待审核文章
        Page<com.zk.projectboot.model.Article> articlePage = articleService.getArticlesByStatus(
                com.zk.projectboot.model.Article.ArticleStatus.pending,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        List<ArticleDTO> articleDTOs = articlePage.getContent().stream()
                .map(ArticleDTO::fromArticle)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(articleDTOs));
    }

    @DeleteMapping("/articles/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteArticle(@PathVariable Integer id) {
        articleApplicationService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("文章已删除", null));
    }

    @PatchMapping("/articles/{id}/publish")
    public ResponseEntity<ApiResponse<ArticleDTO>> publishArticle(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("文章已发布",
                ArticleDTO.fromArticle(articleApplicationService.publish(id))));
    }

    @PatchMapping("/articles/{id}/reject")
    public ResponseEntity<ApiResponse<ArticleDTO>> rejectArticle(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("文章审核已拒绝",
                ArticleDTO.fromArticle(articleApplicationService.reject(id))));
    }

    @PatchMapping("/articles/{id}/featured")
    public ResponseEntity<ApiResponse<ArticleDTO>> setArticleFeatured(
            @PathVariable Integer id,
            @RequestParam Boolean featured) {
        if (articleService.setArticleFeatured(id, featured)) {
            Optional<com.zk.projectboot.model.Article> articleOptional = articleService.getArticleById(id);
            return ResponseEntity.ok(ApiResponse.success(
                    featured ? "文章已设置为推荐" : "文章已取消推荐",
                    ArticleDTO.fromArticle(articleOptional.get())));
        }
        return ResponseEntity.ok(ApiResponse.fail("文章不存在或操作失败"));
    }

    @PatchMapping("/articles/{id}/top")
    public ResponseEntity<ApiResponse<ArticleDTO>> setArticleTop(
            @PathVariable Integer id,
            @RequestParam Boolean top) {
        if (articleService.setArticleTop(id, top)) {
            Optional<com.zk.projectboot.model.Article> articleOptional = articleService.getArticleById(id);
            return ResponseEntity.ok(ApiResponse.success(
                    top ? "文章已设置为置顶" : "文章已取消置顶",
                    ArticleDTO.fromArticle(articleOptional.get())));
        }
        return ResponseEntity.ok(ApiResponse.fail("文章不存在或操作失败"));
    }

    // 标签管理
    @GetMapping("/tags/pending")
    public ResponseEntity<ApiResponse<List<TagDTO>>> getPendingTags() {
        List<Tag> pendingTags = tagService.getPendingTags();

        List<TagDTO> tagDTOs = pendingTags.stream()
                .map(TagDTO::fromTag)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(tagDTOs));
    }

    @PatchMapping("/tags/{id}/approve")
    public ResponseEntity<ApiResponse<TagDTO>> approveTag(@PathVariable Integer id) {
        if (tagService.approveTag(id)) {
            Optional<Tag> tagOptional = tagService.getTagById(id);
            if (tagOptional.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(
                        "标签已批准",
                        TagDTO.fromTag(tagOptional.get())));
            }
        }
        return ResponseEntity.ok(ApiResponse.fail("标签不存在或操作失败"));
    }

    @PatchMapping("/tags/{id}/reject")
    public ResponseEntity<ApiResponse<TagDTO>> rejectTag(@PathVariable Integer id) {
        if (tagService.rejectTag(id)) {
            Optional<Tag> tagOptional = tagService.getTagById(id);
            if (tagOptional.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(
                        "标签已拒绝",
                        TagDTO.fromTag(tagOptional.get())));
            }
        }
        return ResponseEntity.ok(ApiResponse.fail("标签不存在或操作失败"));
    }

    // 分类管理
    @GetMapping("/categories/pending")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getPendingCategories() {
        List<Category> pendingCategories = categoryService.getPendingCategories();

        List<CategoryDTO> categoryDTOs = pendingCategories.stream()
                .map(CategoryDTO::fromCategory)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(categoryDTOs));
    }

    @PatchMapping("/categories/{id}/approve")
    public ResponseEntity<ApiResponse<CategoryDTO>> approveCategory(@PathVariable Integer id) {
        if (categoryService.approveCategory(id)) {
            Optional<Category> categoryOptional = categoryService.getCategoryById(id);
            if (categoryOptional.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(
                        "分类已批准",
                        CategoryDTO.fromCategory(categoryOptional.get())));
            }
        }
        return ResponseEntity.ok(ApiResponse.fail("分类不存在或操作失败"));
    }

    @PatchMapping("/categories/{id}/reject")
    public ResponseEntity<ApiResponse<CategoryDTO>> rejectCategory(@PathVariable Integer id) {
        if (categoryService.rejectCategory(id)) {
            Optional<Category> categoryOptional = categoryService.getCategoryById(id);
            if (categoryOptional.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(
                        "分类已拒绝",
                        CategoryDTO.fromCategory(categoryOptional.get())));
            }
        }
        return ResponseEntity.ok(ApiResponse.fail("分类不存在或操作失败"));
    }
}
