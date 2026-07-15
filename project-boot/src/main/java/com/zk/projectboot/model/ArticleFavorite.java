package com.zk.projectboot.model;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文章收藏实体类
 * 用于记录用户对文章的收藏操作
 * 存储用户与文章之间的收藏关系
 */
@Data                // Lombok注解，自动生成getter、setter、equals、hashCode和toString方法
@Entity              // JPA注解，表示该类是一个实体类，将映射到数据库表
@Table(name = "article_favorites", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"article_id", "user_id"})  // 确保用户对同一篇文章只能收藏一次
})
@NoArgsConstructor   // Lombok注解，生成无参构造函数
@AllArgsConstructor  // Lombok注解，生成包含所有字段的构造函数
public class ArticleFavorite {

    /**
     * 收藏记录ID，主键
     * 使用自增长策略
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 被收藏的文章，多对一关系
     * 即一篇文章可以被多个用户收藏
     * FetchType.LAZY表示延迟加载，只有在实际使用时才会加载文章信息
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    /**
     * 执行收藏操作的用户，多对一关系
     * 即一个用户可以收藏多篇文章
     * FetchType.LAZY表示延迟加载，只有在实际使用时才会加载用户信息
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 收藏创建时间
     * 记录用户收藏文章的时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 实体创建前的钩子方法
     * 自动设置创建时间为当前时间
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
