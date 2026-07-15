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
 * 文章实体类
 * 用于存储系统中的博客文章信息
 * 包含文章的基本信息、分类、作者、标签关联等
 */
@Data                // Lombok注解，自动生成getter、setter、equals、hashCode和toString方法
@Entity              // JPA注解，表示该类是一个实体类，将映射到数据库表
@Table(name = "articles")  // 指定映射的数据库表名为articles
@NoArgsConstructor   // Lombok注解，生成无参构造函数
@AllArgsConstructor  // Lombok注解，生成包含所有字段的构造函数
public class Article {
    /**
     * 文章ID，主键
     * 使用自增长策略
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 文章标题，不能为空
     */
    @Column(nullable = false)
    private String title;

    /**
     * 文章别名/URL友好标识，不能为空且唯一
     * 用于SEO友好的URL
     */
    @Column(nullable = false, unique = true)
    private String slug;

    /**
     * 文章正文内容，不能为空
     * 使用longtext类型存储大文本
     */
    @Column(nullable = false, columnDefinition = "longtext")
    private String content;

    /**
     * 文章摘要/简介
     * 使用text类型存储较长文本
     */
    @Column(columnDefinition = "text")
    private String excerpt;

    /**
     * 文章封面图片URL
     */
    @Column(name = "cover_image")
    private String coverImage;

    /**
     * 文章作者，多对一关系
     * 即一个用户可以发表多篇文章，一篇文章只能有一个作者
     * FetchType.EAGER表示加载文章时立即加载作者信息
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /**
     * 文章分类，多对一关系
     * 即一个分类可以包含多篇文章，一篇文章只能属于一个分类
     * FetchType.EAGER表示加载文章时立即加载分类信息
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /**
     * 文章状态枚举
     * 可以是草稿、已发布或已归档
     */
    @Enumerated(EnumType.STRING)
    private ArticleStatus status;

    /**
     * 文章浏览次数
     */
    @Column(name = "view_count")
    private Integer viewCount;

    /**
     * 文章点赞数量
     */
    @Column(name = "like_count")
    private Integer likeCount;

    /**
     * 文章评论数量
     */
    @Column(name = "comment_count")
    private Integer commentCount;

    /**
     * 是否为特色文章
     * 可用于首页推荐等功能
     */
    @Column(name = "is_featured")
    private Boolean isFeatured;

    /**
     * 是否置顶
     * 可用于文章列表排序
     */
    @Column(name = "is_top")
    private Boolean isTop;

    /**
     * 文章发布时间
     */
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    /**
     * 文章创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 文章最后更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 文章标签关联列表，一对多关系
     * 通过中间表ArticleTag实现文章和标签的多对多关系
     * CascadeType.ALL表示对文章的所有操作都会级联到这些关联
     */
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL)
    private List<ArticleTag> articleTags = new ArrayList<>();

    /**
     * 文章评论列表，一对多关系
     * 即一篇文章可以有多条评论
     * CascadeType.ALL表示对文章的所有操作都会级联到这些评论
     */
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    /**
     * 文章点赞记录列表，一对多关系
     * 用于记录哪些用户点赞了该文章
     */
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL)
    private List<ArticleLike> likes = new ArrayList<>();

    /**
     * 文章收藏记录列表，一对多关系
     * 用于记录哪些用户收藏了该文章
     */
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL)
    private List<ArticleFavorite> favorites = new ArrayList<>();

    /**
     * 标签ID列表，用于接收前端传来的标签ID
     * 不映射到数据库
     */
    @Transient
    private List<Integer> tagIds;

    /**
     * 实体创建前的钩子方法
     * 用于设置默认值并确保必要字段有值
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.viewCount = this.viewCount == null ? 0 : this.viewCount;
        this.likeCount = this.likeCount == null ? 0 : this.likeCount;
        this.commentCount = this.commentCount == null ? 0 : this.commentCount;
        this.isFeatured = this.isFeatured == null ? false : this.isFeatured;
        this.isTop = this.isTop == null ? false : this.isTop;
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
     * 文章状态枚举类
     * 用于表示文章的不同状态
     */
    public enum ArticleStatus {
        draft,       // 草稿状态，尚未提交审核
        pending,     // 审核中状态，已提交等待管理员审核
        published    // 已发布状态，审核通过并发布
    }
}
