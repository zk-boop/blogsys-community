package com.zk.projectboot.service;

import com.zk.projectboot.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 分类服务接口
 * 定义了与分类相关的所有业务操作
 * 包括分类的CRUD、层级结构查询等功能
 */
public interface CategoryService {

    /**
     * 保存新分类
     *
     * @param category 要保存的分类对象
     * @return 保存后的分类对象（包含生成的ID等信息）
     */
    Category saveCategory(Category category);

    /**
     * 通过ID获取分类
     *
     * @param id 分类ID
     * @return 包含分类对象的Optional，如果不存在则为空
     */
    Optional<Category> getCategoryById(Integer id);

    /**
     * 通过名称获取分类
     *
     * @param name 分类名称
     * @return 包含分类对象的Optional，如果不存在则为空
     */
    Optional<Category> getCategoryByName(String name);

    /**
     * 获取所有分类
     *
     * @return 所有分类的列表
     */
    List<Category> getAllCategories();

    /**
     * 获取所有已批准的分类
     * @return 已批准的分类列表
     */
    List<Category> getAllApprovedCategories();

    /**
     * 获取所有待审核的分类
     * @return 待审核的分类列表
     */
    List<Category> getPendingCategories();

    /**
     * 审核分类 - 批准
     * @param id 分类ID
     * @return 操作是否成功
     */
    boolean approveCategory(Integer id);

    /**
     * 审核分类 - 拒绝
     * @param id 分类ID
     * @return 操作是否成功
     */
    boolean rejectCategory(Integer id);

    /**
     * 获取所有根分类（没有父分类的分类）
     *
     * @return 根分类列表
     */
    List<Category> getRootCategories();

    /**
     * 获取所有已批准的根分类
     * @return 已批准的根分类列表
     */
    List<Category> getApprovedRootCategories();

    /**
     * 获取指定父分类下的所有子分类
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<Category> getSubcategoriesByParentId(Integer parentId);

    /**
     * 获取指定父分类下的所有已批准的子分类
     *
     * @param parentId 父分类ID
     * @return 已批准的子分类列表
     */
    List<Category> getApprovedSubcategoriesByParentId(Integer parentId);

    /**
     * 更新分类信息
     *
     * @param category 更新后的分类对象
     * @return 更新后保存的分类对象
     */
    Category updateCategory(Category category);

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 操作是否成功
     */
    boolean deleteCategory(Integer id);

    /**
     * 检查指定名称的分类是否存在
     *
     * @param name 分类名称
     * @return 是否存在
     */
    boolean existsByName(String name);
}
