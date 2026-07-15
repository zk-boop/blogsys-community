package com.zk.projectboot.model;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 标签实体类
 * 用于存储系统中的标签信息
 * 标签可以被添加到文章上，用于文章分类和搜索
 */
@Data                // Lombok注解，自动生成getter、setter、equals、hashCode和toString方法
@Entity              // JPA注解，表示该类是一个实体类，将映射到数据库表
@Table(name = "tags")  // 指定映射的数据库表名为tags
@NoArgsConstructor   // Lombok注解，生成无参构造函数
@AllArgsConstructor  // Lombok注解，生成包含所有字段的构造函数
public class Tag {

    /**
     * 标签ID，主键
     * 使用自增长策略
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 标签名称，不可为空且唯一
     */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * 标签描述
     * 用于详细说明标签的用途或含义
     */
    private String description;

    /**
     * 标签颜色
     * 用于在前端显示时的颜色标识，通常使用16进制颜色代码
     */
    private String color;

    /**
     * 标签状态
     * 用于标识标签的审核状态：
     * 1表示已批准可正常使用
     * 0表示待审核状态
     * 2表示已拒绝状态
     */
    @Column
    private Integer status;

    /**
     * 标签创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 标签最后更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 标签与文章的关联列表，一对多关系
     * 通过ArticleTag中间表实现标签与文章的多对多关系
     */
    @OneToMany(mappedBy = "tag")
    private List<ArticleTag> articleTags = new ArrayList<>();

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
            this.status = 0; // 待审核
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
