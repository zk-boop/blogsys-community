package com.zk.projectboot.service.impl;

import com.zk.projectboot.model.Category;
import com.zk.projectboot.repository.CategoryRepository;
import com.zk.projectboot.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.zk.projectboot.model.Article;

/**
 * 分类服务接口实现类
 * 实现CategoryService接口中定义的所有方法
 * 提供分类的增删改查、层级结构查询等业务逻辑
 */
@Service  // 标记为Spring服务组件
public class CategoryServiceImpl implements CategoryService {

    /**
     * 分类数据访问层接口
     * 用于操作分类数据
     */
    private final CategoryRepository categoryRepository;

    /**
     * 构造函数注入依赖的仓库组件
     *
     * @param categoryRepository 分类仓库
     */
    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * 保存新分类
     * 使用@Transactional确保操作的原子性
     *
     * @param category 分类对象
     * @return 保存后的分类对象
     */
    @Override
    @Transactional
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    /**
     * 通过ID获取分类
     *
     * @param id 分类ID
     * @return 包含分类对象的Optional，如果不存在则为空
     */
    @Override
    public Optional<Category> getCategoryById(Integer id) {
        return categoryRepository.findById(id);
    }

    /**
     * 通过名称获取分类
     *
     * @param name 分类名称
     * @return 包含分类对象的Optional，如果不存在则为空
     */
    @Override
    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    /**
     * 获取所有分类
     * 按排序值排序
     *
     * @return 所有分类的列表
     */
    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAllOrderBySortOrder();
    }

    /**
     * 获取所有已批准的分类
     * @return 已批准的分类列表
     */
    @Override
    public List<Category> getAllApprovedCategories() {
        return categoryRepository.findAll().stream()
            .filter(category -> "1".equals(category.getStatus()))
            .collect(Collectors.toList());
    }

    /**
     * 获取所有待审核的分类
     * @return 待审核的分类列表
     */
    @Override
    public List<Category> getPendingCategories() {
        return categoryRepository.findAll().stream()
            .filter(category -> "PENDING".equals(category.getStatus()) || "0".equals(category.getStatus()))
            .collect(Collectors.toList());
    }

    /**
     * 审核分类 - 批准
     * @param id 分类ID
     * @return 操作是否成功
     */
    @Override
    @Transactional
    public boolean approveCategory(Integer id) {
        Optional<Category> categoryOptional = categoryRepository.findById(id);
        if (categoryOptional.isPresent()) {
            Category category = categoryOptional.get();
            category.setStatus("1");  // 设置为已批准
            categoryRepository.save(category);
            return true;
        }
        return false;
    }

    /**
     * 审核分类 - 拒绝
     * @param id 分类ID
     * @return 操作是否成功
     */
    @Override
    @Transactional
    public boolean rejectCategory(Integer id) {
        Optional<Category> categoryOptional = categoryRepository.findById(id);
        if (categoryOptional.isPresent()) {
            // 获取分类下的所有文章
            Category category = categoryOptional.get();
            List<Article> articles = category.getArticles();

            if (articles != null && !articles.isEmpty()) {
                // 获取"其他"分类（假设ID为8）
                Optional<Category> otherCategoryOptional = categoryRepository.findById(8);
                if (!otherCategoryOptional.isPresent()) {
                    return false;
                }

                Category otherCategory = otherCategoryOptional.get();

                // 将所有关联的文章分类设置为"其他"
                for (Article article : articles) {
                    article.setCategory(otherCategory);
                }
            }

            // 删除被拒绝的分类
            categoryRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * 获取所有根分类（没有父分类的分类）
     *
     * @return 根分类列表
     */
    @Override
    public List<Category> getRootCategories() {
        return categoryRepository.findByParentIdIsNull();
    }

    /**
     * 获取所有已批准的根分类
     * @return 已批准的根分类列表
     */
    @Override
    public List<Category> getApprovedRootCategories() {
        return categoryRepository.findByParentIdIsNull().stream()
            .filter(category -> "1".equals(category.getStatus()))
            .collect(Collectors.toList());
    }

    /**
     * 获取指定父分类下的所有子分类
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    @Override
    public List<Category> getSubcategoriesByParentId(Integer parentId) {
        return categoryRepository.findByParentId(parentId);
    }

    /**
     * 获取指定父分类下的所有已批准的子分类
     *
     * @param parentId 父分类ID
     * @return 已批准的子分类列表
     */
    @Override
    public List<Category> getApprovedSubcategoriesByParentId(Integer parentId) {
        return categoryRepository.findByParentId(parentId).stream()
            .filter(category -> "1".equals(category.getStatus()))
            .collect(Collectors.toList());
    }

    /**
     * 更新分类信息
     * 使用@Transactional确保操作的原子性
     *
     * @param category 更新后的分类对象
     * @return 更新后保存的分类对象
     */
    @Override
    @Transactional
    public Category updateCategory(Category category) {
        return categoryRepository.save(category);
    }

    /**
     * 删除分类
     * 使用@Transactional确保操作的原子性
     * 先检查分类是否存在，然后进行删除
     * 如果分类下有关联的文章，先将这些文章的分类设置为"其他"，然后再删除分类
     *
     * @param id 分类ID
     * @return 操作是否成功
     */
    @Override
    @Transactional
    public boolean deleteCategory(Integer id) {
        Optional<Category> categoryOptional = categoryRepository.findById(id);
        if (!categoryOptional.isPresent()) {
            return false;
        }

        Category category = categoryOptional.get();

        // 检查是否有文章与此分类关联
        if (category.getArticles() != null && !category.getArticles().isEmpty()) {
            // 获取"其他"分类（假设ID为1，实际使用时应该从配置或数据库获取）
            Optional<Category> otherCategoryOptional = categoryRepository.findById(1);
            if (!otherCategoryOptional.isPresent()) {
                // 如果"其他"分类不存在，可以考虑创建一个
                return false;
            }

            Category otherCategory = otherCategoryOptional.get();

            // 将所有关联的文章分类设置为"其他"
            for (Article article : category.getArticles()) {
                article.setCategory(otherCategory);
                // 不需要显式保存article，因为有级联关系
            }
        }

        // 删除分类
        categoryRepository.deleteById(id);
        return true;
    }

    /**
     * 检查指定名称的分类是否存在
     * 用于创建和更新分类时验证名称唯一性
     *
     * @param name 分类名称
     * @return 是否存在
     */
    @Override
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }
}
