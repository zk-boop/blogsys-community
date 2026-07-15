package com.zk.projectboot.model;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文章-标签关联实体类
 * 用于存储文章和标签之间的多对多关系
 * 作为文章和标签之间的中间表，实现多对多关系
 */
@Data                // Lombok注解，自动生成getter、setter、equals、hashCode和toString方法
@Entity              // JPA注解，表示该类是一个实体类，将映射到数据库表
@Table(name = "article_tags", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"article_id", "tag_id"})  // 确保文章-标签的组合唯一，避免重复关联
})
@NoArgsConstructor   // Lombok注解，生成无参构造函数
@AllArgsConstructor  // Lombok注解，生成包含所有字段的构造函数
public class ArticleTag {

    /**
     * 关联记录ID，主键
     * 使用自增长策略
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 关联的文章，多对一关系
     * FetchType.LAZY表示延迟加载，只有在实际使用时才会加载文章信息
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    /**
     * 关联的标签，多对一关系
     * FetchType.LAZY表示延迟加载，只有在实际使用时才会加载标签信息
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    /**
     * 关联创建时间
     * 记录文章与标签关联的创建时间
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
