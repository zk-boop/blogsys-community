package com.zk.projectboot.model;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色实体类
 * 用于存储系统中的角色信息
 * 角色用于权限管理，确定用户在系统中的访问权限
 */
@Data                // Lombok注解，自动生成getter、setter、equals、hashCode和toString方法
@Entity              // JPA注解，表示该类是一个实体类，将映射到数据库表
@Table(name = "roles")  // 指定映射的数据库表名为roles
@NoArgsConstructor   // Lombok注解，生成无参构造函数
@AllArgsConstructor  // Lombok注解，生成包含所有字段的构造函数
public class Role implements Serializable {
    /**
     * 序列化版本UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * 角色ID，主键
     * 使用自增长策略
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 角色名称，不可为空且唯一
     * 例如：ROLE_ADMIN, ROLE_USER等
     */
    @Column(name = "role_name", unique = true, nullable = false)
    private String roleName;

    /**
     * 角色描述
     * 用于说明该角色的职责和权限范围
     */
    @Column(name = "role_description")
    private String roleDescription;

    /**
     * 角色权限列表
     * 使用text类型存储较长文本，通常以JSON格式存储权限信息
     */
    @Column(name = "permissions", columnDefinition = "text")
    private String permissions;

    /**
     * 角色创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 角色最后更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 与该角色关联的用户列表，一对多关系
     * 即一个角色可以分配给多个用户
     */
    @OneToMany(mappedBy = "role")
    private java.util.List<User> users;

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
}
