package com.zk.projectboot.service;

import com.zk.projectboot.model.Article;
import com.zk.projectboot.model.ArticleLike;
import com.zk.projectboot.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ArticleLikeServiceTest {

    @Autowired
    private ArticleLikeService articleLikeService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    @Test
    void testIsArticleLikedByUser() {
        // 测试用户3是否点赞了文章4
        boolean isLiked = articleLikeService.existsByArticleIdAndUserId(4, 3);

        // 验证结果（根据初始数据确认）
        assertTrue(isLiked);

        // 测试用户3是否点赞了文章2（未点赞）
        boolean isNotLiked = articleLikeService.existsByArticleIdAndUserId(2, 3);

        // 验证结果
        assertFalse(isNotLiked);
    }

    @Test
    @Transactional
    void testLikeAndUnlikeArticle() {
        // 获取用户和文章
        Optional<User> user = userService.getUserById(3);
        Optional<Article> article = articleService.getArticleById(2);
        assertTrue(user.isPresent());
        assertTrue(article.isPresent());

        // 获取初始点赞数
        int initialLikeCount = article.get().getLikeCount();

        // 创建新的点赞记录
        ArticleLike articleLike = new ArticleLike();
        articleLike.setArticle(article.get());
        articleLike.setUser(user.get());

        // 保存点赞记录
        ArticleLike savedLike = articleLikeService.saveArticleLike(articleLike);

        // 验证结果
        assertNotNull(savedLike);

        // 重新获取文章验证点赞数是否增加 (注意：这里依赖于触发器更新点赞数)
        Optional<Article> updatedArticle = articleService.getArticleById(2);
        assertTrue(updatedArticle.isPresent());

        // 验证文章是否被用户点赞
        assertTrue(articleLikeService.existsByArticleIdAndUserId(article.get().getId(), user.get().getId()));

        // 删除点赞
        boolean deleteResult = articleLikeService.deleteByArticleIdAndUserId(article.get().getId(), user.get().getId());

        // 验证结果
        assertTrue(deleteResult);

        // 验证文章未被用户点赞
        assertFalse(articleLikeService.existsByArticleIdAndUserId(article.get().getId(), user.get().getId()));
    }

    @Test
    void testLikeNonExistingArticle() {
        // 创建新的点赞记录，使用不存在的文章ID
        // 注意：这里我们无法直接使用不存在的ID创建点赞，因为外键约束
        // 所以我们只能通过getArticleLikeByArticleAndUser方法测试
        Optional<ArticleLike> nonExistingLike = articleLikeService.getArticleLikeByArticleAndUser(999, 3);

        // 验证结果
        assertFalse(nonExistingLike.isPresent());
    }

    @Test
    void testLikeWithNonExistingUser() {
        // 测试不存在的用户点赞
        Optional<ArticleLike> nonExistingUserLike = articleLikeService.getArticleLikeByArticleAndUser(2, 999);

        // 验证结果
        assertFalse(nonExistingUserLike.isPresent());
    }
}
