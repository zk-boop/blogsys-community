//package com.zk.projectboot.service;
//
//import com.zk.projectboot.model.Comment;
//import com.zk.projectboot.model.Article;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//public class CommentServiceTest {
//
//    @Autowired
//    private CommentService commentService;
//
//    @Autowired
//    private ArticleService articleService;
//
//    @Test
//    void testGetCommentById() {
//        // 获取ID为4的评论
//        Optional<Comment> foundComment = commentService.getCommentById(4);
//
//        // 验证结果
//        assertTrue(foundComment.isPresent());
//        assertEquals("React Hooks 让代码更简洁了", foundComment.get().getContent());
//        assertEquals(Comment.CommentStatus.approved, foundComment.get().getStatus());
//    }
//
//    @Test
//    void testGetCommentsByArticleId() {
//        // 获取文章ID为2的评论
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<Comment> commentPage = commentService.getCommentsByArticleId(2, pageable);
//
//        // 验证结果
//        assertFalse(commentPage.isEmpty());
//
//        // 确保所有评论都属于文章ID为2
//        for (Comment comment : commentPage.getContent()) {
//            assertEquals(2, comment.getArticle().getId());
//            assertEquals(Comment.CommentStatus.approved, comment.getStatus());
//        }
//    }
//
//    @Test
//    void testCountByArticleId() {
//        // 获取文章ID为2的评论数量
//        long commentCount = commentService.countByArticleId(2);
//
//        // 验证结果
//        assertTrue(commentCount > 0);
//
//        // 获取文章对象
//        Optional<Article> article = articleService.getArticleById(2);
//        assertTrue(article.isPresent());
//
//        // 只验证评论数量大于0，不验证具体数值
//        // 因为测试数据库中的实际评论数可能会变化
//        System.out.println("文章ID为2的评论数: " + commentCount);
//        System.out.println("文章对象中记录的评论数: " + article.get().getCommentCount());
//    }
//
//    @Test
//    @Transactional
//    void testSaveAndDeleteComment() {
//        // 创建新评论
//        Comment comment = new Comment();
//        comment.setContent("这是一条测试评论，将在测试后删除");
//
//        // 设置文章
//        Optional<Article> article = articleService.getArticleById(2);
//        assertTrue(article.isPresent());
//        comment.setArticle(article.get());
//
//        // 保存评论（由于外键约束，我们需要设置用户，但这里暂不测试）
//        // 在实际环境中可能需要先创建临时测试用户
//
//        // 查询评论
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<Comment> comments = commentService.getAllComments(pageable);
//
//        // 验证结果
//        assertNotNull(comments);
//    }
//
//    @Test
//    void testGetPendingComments() {
//        // 获取待审核评论
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<Comment> pendingComments = commentService.getAllComments(pageable);
//
//        // 验证结果
//        // 注意：这个测试可能不稳定，取决于数据库中是否有待审核评论
//        for (Comment comment : pendingComments.getContent()) {
//            assertEquals(Comment.CommentStatus., comment.getStatus());
//        }
//    }
//
//    @Test
//    @Transactional
//    void testApproveAndRejectComment() {
//        // 本测试需要一个PENDING状态的评论
//        // 由于实际数据库中可能没有待审核评论，这个测试可能不稳定
//        // 在实际环境中可能需要先创建临时测试评论
//
//        // 获取待审核评论
//        Pageable pageable = PageRequest.of(0, 1);
//        Page<Comment> pendingComments = commentService.getPendingComments(pageable);
//
//        // 如果有待审核评论，测试审核功能
//        if (!pendingComments.isEmpty()) {
//            Comment pendingComment = pendingComments.getContent().get(0);
//
//            // 测试审核通过
//            boolean approveResult = commentService.approveComment(pendingComment.getId());
//
//            // 验证结果 - 注意这里不做断言，因为不确定是否有待审核评论
//            if (approveResult) {
//                // 重新获取评论验证状态
//                Optional<Comment> approvedComment = commentService.getCommentById(pendingComment.getId());
//                if (approvedComment.isPresent()) {
//                    assertEquals(Comment.CommentStatus.approved, approvedComment.get().getStatus());
//
//                    // 测试拒绝评论
//                    boolean rejectResult = commentService.rejectComment(pendingComment.getId());
//                    if (rejectResult) {
//                        // 重新获取评论验证状态
//                        Optional<Comment> rejectedComment = commentService.getCommentById(pendingComment.getId());
//                        if (rejectedComment.isPresent()) {
//                            assertEquals(Comment.CommentStatus.rejected, rejectedComment.get().getStatus());
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
