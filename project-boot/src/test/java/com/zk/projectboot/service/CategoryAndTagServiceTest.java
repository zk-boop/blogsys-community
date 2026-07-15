package com.zk.projectboot.service;

import com.zk.projectboot.model.Category;
import com.zk.projectboot.model.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CategoryAndTagServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TagService tagService;

    @Test
    void testGetAllCategories() {
        // 获取所有分类
        List<Category> categories = categoryService.getAllCategories();

        // 验证结果
        assertFalse(categories.isEmpty());
        assertTrue(categories.size() >= 8); // 根据初始数据，至少有8个分类
    }

    @Test
    void testGetCategoryById() {
        // 获取ID为2的分类（前端开发）
        Optional<Category> category = categoryService.getCategoryById(2);

        // 验证结果
        assertTrue(category.isPresent());
        assertEquals("前端开发", category.get().getName());
        assertEquals(1, category.get().getParentId()); // 父分类是技术
    }

    @Test
    void testGetCategoriesByParentId() {
        // 获取父分类ID为1（技术）的所有子分类
        List<Category> childCategories = categoryService.getSubcategoriesByParentId(1);

        // 验证结果
        assertFalse(childCategories.isEmpty());
        assertTrue(childCategories.size() >= 3); // 至少有3个子分类

        // 验证所有返回的分类的父ID都是1
        for (Category category : childCategories) {
            assertEquals(1, category.getParentId());
        }
    }

    @Test
    void testGetAllTags() {
        // 获取所有标签
        List<Tag> tags = tagService.getAllTags();

        // 验证结果
        assertFalse(tags.isEmpty());
        assertTrue(tags.size() >= 10); // 根据初始数据，至少有10个标签
    }

    @Test
    void testGetTagById() {
        // 获取ID为5的标签（JavaScript）
        Optional<Tag> tag = tagService.getTagById(5);

        // 验证结果
        assertTrue(tag.isPresent());
        assertEquals("JavaScript", tag.get().getName());
        assertEquals("#f7df1e", tag.get().getColor());
    }

    @Test
    @Transactional
    void testCreateAndUpdateCategory() {
        // 创建新的分类
        Category newCategory = new Category();
        newCategory.setName("测试分类");
        newCategory.setDescription("这是一个测试分类");
        newCategory.setParentId(1); // 父分类为技术
        newCategory.setSortOrder(10);
        newCategory.setCreatedAt(LocalDateTime.now());
        newCategory.setUpdatedAt(LocalDateTime.now());

        // 保存分类
        Category savedCategory = categoryService.saveCategory(newCategory);

        // 验证结果
        assertNotNull(savedCategory.getId());
        assertEquals("测试分类", savedCategory.getName());

        // 更新分类
        savedCategory.setDescription("这是更新后的测试分类描述");
        Category updatedCategory = categoryService.updateCategory(savedCategory);

        // 验证结果
        assertEquals("这是更新后的测试分类描述", updatedCategory.getDescription());

        // 删除测试分类
        boolean deleteResult = categoryService.deleteCategory(savedCategory.getId());
        assertTrue(deleteResult);
    }

    @Test
    @Transactional
    void testCreateAndUpdateTag() {
        // 创建新的标签
        Tag newTag = new Tag();
        newTag.setName("测试标签");
        newTag.setDescription("这是一个测试标签");
        newTag.setColor("#123ABC");
        newTag.setCreatedAt(LocalDateTime.now());
        newTag.setUpdatedAt(LocalDateTime.now());

        // 保存标签
        Tag savedTag = tagService.saveTag(newTag);

        // 验证结果
        assertNotNull(savedTag.getId());
        assertEquals("测试标签", savedTag.getName());
        assertEquals("#123ABC", savedTag.getColor());

        // 更新标签
        savedTag.setDescription("这是更新后的测试标签描述");
        savedTag.setColor("#654321");
        Tag updatedTag = tagService.updateTag(savedTag);

        // 验证结果
        assertEquals("这是更新后的测试标签描述", updatedTag.getDescription());
        assertEquals("#654321", updatedTag.getColor());

        // 删除测试标签
        boolean deleteResult = tagService.deleteTag(savedTag.getId());
        assertTrue(deleteResult);
    }
}
