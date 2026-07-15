package com.zk.projectboot.model;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户实体类
 * 用于存储系统中的用户信息
 * 包含用户的基本信息、角色、状态以及与其他实体的关系
 */
@Data                // Lombok注解，自动生成getter、setter、equals、hashCode和toString方法
@Entity              // JPA注解，表示该类是一个实体类，将映射到数据库表
@Table(name = "users")  // 指定映射的数据库表名为users
@NoArgsConstructor   // Lombok注解，生成无参构造函数
@AllArgsConstructor  // Lombok注解，生成包含所有字段的构造函数
public class User implements Serializable {
    /**
     * 序列化版本UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID，主键
     * 使用自增长策略
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 用户名，不可为空且唯一
     * 用于用户登录
     */
    @Column(nullable = false, unique = true)
    private String username;

    /**
     * 电子邮箱，不可为空且唯一
     * 用于用户联系和找回密码等功能
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * 密码哈希，不可为空
     * 存储加密后的密码，而非明文
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * 用户昵称
     * 用于在站点上显示
     */
    private String nickname;

    /**
     * 用户头像URL
     * 存储头像图片的路径
     */
    private String avatar;

    /**
     * 用户个人简介
     * 使用text类型存储较长文本
     */
    @Column(columnDefinition = "text")
    private String bio;

    /**
     * 用户角色，多对一关系
     * 即一个角色可以分配给多个用户，一个用户只能拥有一个角色
     * FetchType.EAGER表示加载用户时立即加载角色信息
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    /**
     * 用户状态枚举
     * 可以是活跃、非活跃或被禁用
     */
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "retention_policy", nullable = false, length = 16)
    private RetentionPolicy retentionPolicy = RetentionPolicy.EPHEMERAL;

    /**
     * 用户最后登录时间
     */
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    /**
     * 用户创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 用户信息最后更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 用户发表的文章列表，一对多关系
     * 即一个用户可以发表多篇文章
     * CascadeType.ALL表示对用户的所有操作都会级联到这些文章
     */
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Article> articles = new ArrayList<>();

    /**
     * 用户发表的评论列表，一对多关系
     * 即一个用户可以发表多条评论
     * CascadeType.ALL表示对用户的所有操作都会级联到这些评论
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    /**
     * 用户点赞的文章记录列表，一对多关系
     * 记录用户点赞过的所有文章
     * CascadeType.ALL表示对用户的所有操作都会级联到这些点赞记录
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ArticleLike> articleLikes = new ArrayList<>();

    /**
     * 用户收藏的文章记录列表，一对多关系
     * 记录用户收藏过的所有文章
     * CascadeType.ALL表示对用户的所有操作都会级联到这些收藏记录
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ArticleFavorite> articleFavorites = new ArrayList<>();

    /**
     * 实体创建前的钩子方法
     * 自动设置创建时间和更新时间为当前时间
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 实体更新前的钩子方法
     * 自动更新最后修改时间为当前时间
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 用户状态枚举类
     * 用于表示用户的不同状态
     */
    public enum UserStatus {
        active,    // 活跃状态，正常使用
        inactive,  // 非活跃状态，可能需要激活
        banned     // 被禁用状态，不能登录和使用系统
    }

    public enum RetentionPolicy {
        PERMANENT,
        EPHEMERAL
    }

    /**
     * 获取密码
     * 为了兼容Spring Security，提供getPassword方法
     * @return 加密后的密码哈希
     */
    public String getPassword() {
        return this.passwordHash;
    }

    /**
     * 设置密码
     * 为了兼容Spring Security，提供setPassword方法
     * @param password 密码（通常是加密后的）
     */
    public void setPassword(String password) {
        this.passwordHash = password;
    }
}
