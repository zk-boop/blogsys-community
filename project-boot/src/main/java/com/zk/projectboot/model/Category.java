package com.zk.projectboot.model;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 文章分类实体类
 * 用于存储系统中的文章分类信息
 * 支持分层级的分类结构（通过parent和children关系）
 */
@Data                // Lombok注解，自动生成getter、setter、equals、hashCode和toString方法
@Entity              // JPA注解，表示该类是一个实体类，将映射到数据库表
@Table(name = "categories")  // 指定映射的数据库表名为categories
@NoArgsConstructor   // Lombok注解，生成无参构造函数
@AllArgsConstructor  // Lombok注解，生成包含所有字段的构造函数
public class Category {

    /**
     * 分类ID，主键
     * 使用自增长策略
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 分类名称，不可为空且唯一
     */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * 分类描述
     * 用于详细说明分类的用途或内容范围
     */
    private String description;

    /**
     * 父分类ID
     * 用于构建分类的层级结构
     * 如果为null，则表示这是一个顶级分类
     */
    @Column(name = "parent_id")
    private Integer parentId;

    /**
     * 父分类实体，多对一关系
     * 即一个分类可以作为多个分类的父分类
     * FetchType.LAZY表示延迟加载，只有在实际使用时才会加载父分类信息
     * insertable=false, updatable=false表示此关系字段不参与插入和更新操作
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Category parent;

    /**
     * 子分类列表，一对多关系
     * 即一个分类可以包含多个子分类
     */
    @OneToMany(mappedBy = "parent")
    private List<Category> children = new ArrayList<>();

    /**
     * 分类排序顺序
     * 用于在前端显示时的排序，数值越小排序越靠前
     */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /**
     * 分类状态
     * 用于标识分类的审核状态：
     * '1'表示已批准可正常使用
     * 'PENDING'表示待审核状态
     * '0'表示待审核状态（兼容）
     * '2'表示已拒绝状态
     */
    @Column
    private String status;

    /**
     * 分类创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 分类最后更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 该分类下的文章列表，一对多关系
     * 即一个分类可以包含多篇文章
     */
    @OneToMany(mappedBy = "category")
    private List<Article> articles = new ArrayList<>();

    /**
     * 实体创建前的钩子方法
     * 自动设置创建时间和更新时间为当前时间
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // 如果状态为null，默认设置为待审核
        if (this.status == null) {
            this.status = "PENDING";
        }
    }

    /**
     * 实体更新前的钩子方法
     * 自动更新最后修改时间为当前时间
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
