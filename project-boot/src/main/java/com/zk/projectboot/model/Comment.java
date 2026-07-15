package com.zk.projectboot.model;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 评论实体类
 * 用于存储系统中的文章评论信息
 * 支持嵌套评论结构（通过parent和replies关系）
 */
@Data                // Lombok注解，自动生成getter、setter、equals、hashCode和toString方法
@Entity              // JPA注解，表示该类是一个实体类，将映射到数据库表
@Table(name = "comments")  // 指定映射的数据库表名为comments
@NoArgsConstructor   // Lombok注解，生成无参构造函数
@AllArgsConstructor  // Lombok注解，生成包含所有字段的构造函数
public class Comment {
    /**
     * 评论ID，主键
     * 使用自增长策略
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 评论内容，不可为空
     * 使用text类型存储较长文本
     */
    @Column(nullable = false, columnDefinition = "text")
    private String content;

    /**
     * 关联的文章，多对一关系
     * 即一篇文章可以有多条评论，一条评论只能属于一篇文章
     * FetchType.LAZY表示延迟加载，只有在实际使用时才会加载文章信息
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    /**
     * 评论作者，多对一关系
     * 即一个用户可以发表多条评论，一条评论只能有一个作者
     * FetchType.LAZY表示延迟加载，只有在实际使用时才会加载用户信息
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 父评论，多对一关系
     * 用于实现嵌套评论结构，如果为null则表示这是一个顶级评论
     * FetchType.LAZY表示延迟加载，只有在实际使用时才会加载父评论信息
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    /**
     * 子评论（回复）列表，一对多关系
     * 即一条评论可以有多条回复
     */
    @OneToMany(mappedBy = "parent")
    private List<Comment> replies = new ArrayList<>();

    /**
     * 回复的目标用户，多对一关系
     * 用于记录评论是回复给哪个用户的
     * FetchType.LAZY表示延迟加载，只有在实际使用时才会加载用户信息
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_user_id")
    private User replyToUser;

    /**
     * 评论获赞数量
     */
    @Column(name = "like_count")
    private Integer likeCount;

    /**
     * 评论状态枚举
     * 可以是待审核、已批准或已拒绝
     */
    @Enumerated(EnumType.STRING)
    private CommentStatus status;

    /**
     * 评论创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 评论最后更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 实体创建前的钩子方法
     * 自动设置创建时间、更新时间和默认的点赞数
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.likeCount = this.likeCount == null ? 0 : this.likeCount;

        // 默认设置评论状态为已批准，不需要审核
        if (this.status == null) {
            this.status = CommentStatus.approved;
        }
    }

    /**
     * 实体更新前的钩子方法
     * 自动更新最后修改时间
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 评论状态枚举类
     * 用于表示评论的不同状态
     */
    public enum CommentStatus {
        approved,  // 已批准状态，允许显示
        rejected   // 已拒绝状态，不允许显示
    }
}
