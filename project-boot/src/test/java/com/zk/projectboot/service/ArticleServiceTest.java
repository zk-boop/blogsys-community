package com.zk.projectboot.service;

import com.zk.projectboot.model.Article;
import com.zk.projectboot.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ArticleServiceTest {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    @Test
    void testGetArticleById() {
        // 获取ID为2的文章
        Optional<Article> foundArticle = articleService.getArticleById(2);

        // 验证结果
        assertTrue(foundArticle.isPresent());
        assertEquals("React Hooks 使用指南", foundArticle.get().getTitle());
        assertEquals("react-hooks-guide", foundArticle.get().getSlug());
    }

    @Test
    void testGetArticleBySlug() {
        // 通过slug获取文章
        Optional<Article> foundArticle = articleService.getArticleBySlug("react-hooks-guide");

        // 验证结果
        assertTrue(foundArticle.isPresent());
        assertEquals(2, foundArticle.get().getId());
        assertEquals("React Hooks 使用指南", foundArticle.get().getTitle());
    }

    @Test
    void testGetPublishedArticles() {
        // 获取已发布文章
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Article> articlePage = articleService.getPublishedArticles(pageable);

        // 验证结果
        assertFalse(articlePage.isEmpty());

        // 确保所有返回的文章都是已发布状态
        for (Article article : articlePage.getContent()) {
            assertEquals(Article.ArticleStatus.published, article.getStatus());
        }
    }

    @Test
    void testGetArticlesByCategory() {
        // 获取分类ID为2(前端开发)的文章
        Pageable pageable = PageRequest.of(0, 10);
        Page<Article> articlePage = articleService.getArticlesByCategory(2, pageable);

        // 验证结果
        assertFalse(articlePage.isEmpty());

        // 确保所有返回的文章都属于分类ID为2
        for (Article article : articlePage.getContent()) {
            assertEquals(2, article.getCategory().getId());
        }
    }

    @Test
    void testGetArticlesByAuthor() {
        // 获取用户ID为3的作者文章
        Optional<User> author = userService.getUserById(3);
        assertTrue(author.isPresent());

        Pageable pageable = PageRequest.of(0, 10);
        Page<Article> articlePage = articleService.getArticlesByAuthor(author.get(), pageable);

        // 验证结果
        assertFalse(articlePage.isEmpty());

        // 确保所有返回的文章作者ID为3
        for (Article article : articlePage.getContent()) {
            assertEquals(3, article.getAuthor().getId());
        }
    }

    @Test
    void testSearchArticles() {
        // 搜索包含"React"的文章
        String keyword = "React";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Article> articlePage = articleService.searchArticles(keyword, pageable);

        // 验证结果
        assertFalse(articlePage.isEmpty());

        // 检查结果是否与关键词相关
        boolean foundMatch = false;
        for (Article article : articlePage.getContent()) {
            if (article.getTitle().contains(keyword) ||
                (article.getContent() != null && article.getContent().contains(keyword)) ||
                (article.getExcerpt() != null && article.getExcerpt().contains(keyword))) {
                foundMatch = true;
                break;
            }
        }
        assertTrue(foundMatch, "搜索结果中应该至少有一篇文章包含关键词'" + keyword + "'");
    }

    @Test
    void testGetArticlesByTagSupportsPublishedAtSort() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "publishedAt"));

        Page<Article> articlePage = articleService.getArticlesByTag(4, pageable);

        assertEquals(1, articlePage.getTotalElements());
        assertEquals(2, articlePage.getContent().get(0).getId());
        assertEquals(Article.ArticleStatus.published, articlePage.getContent().get(0).getStatus());
    }

    @Test
    @Transactional
    void testIncrementViewCount() {
        // 获取当前文章的浏览次数
        Optional<Article> article = articleService.getArticleById(2);
        assertTrue(article.isPresent());
        int initialViewCount = article.get().getViewCount();

        // 增加浏览次数
        boolean result = articleService.incrementViewCount(2);

        // 验证结果
        assertTrue(result);

        // 重新获取文章检查浏览次数是否增加
        Optional<Article> updatedArticle = articleService.getArticleById(2);
        assertTrue(updatedArticle.isPresent());
        assertEquals(initialViewCount + 1, updatedArticle.get().getViewCount());
    }
}
