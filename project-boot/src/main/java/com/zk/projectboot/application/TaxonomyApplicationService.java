package com.zk.projectboot.application;

import com.zk.projectboot.dto.CategoryWriteRequest;
import com.zk.projectboot.dto.TagWriteRequest;
import com.zk.projectboot.exception.BusinessException;
import com.zk.projectboot.model.Category;
import com.zk.projectboot.model.Tag;
import com.zk.projectboot.security.CurrentUserService;
import com.zk.projectboot.service.CategoryService;
import com.zk.projectboot.service.TagService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaxonomyApplicationService {

    private final CategoryService categoryService;
    private final TagService tagService;
    private final CurrentUserService currentUserService;

    public TaxonomyApplicationService(CategoryService categoryService,
                                      TagService tagService,
                                      CurrentUserService currentUserService) {
        this.categoryService = categoryService;
        this.tagService = tagService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public Category submitCategory(CategoryWriteRequest request) {
        currentUserService.requireCurrentUser();
        validateCategoryName(request.getName(), null);
        validateParent(request.getParentId(), null);
        Category category = new Category();
        apply(category, request);
        category.setStatus("PENDING");
        return categoryService.saveCategory(category);
    }

    @Transactional
    public Category updateCategory(Integer id, CategoryWriteRequest request) {
        currentUserService.requireAdmin();
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "分类不存在"));
        validateCategoryName(request.getName(), category.getName());
        validateParent(request.getParentId(), id);
        apply(category, request);
        return categoryService.updateCategory(category);
    }

    @Transactional
    public void deleteCategory(Integer id) {
        currentUserService.requireAdmin();
        if (!categoryService.deleteCategory(id)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "分类不存在或删除失败");
        }
    }

    @Transactional
    public Tag submitTag(TagWriteRequest request) {
        currentUserService.requireCurrentUser();
        validateTagName(request.getName(), null);
        Tag tag = new Tag();
        apply(tag, request);
        tag.setStatus(0);
        return tagService.saveTag(tag);
    }

    @Transactional
    public Tag updateTag(Integer id, TagWriteRequest request) {
        currentUserService.requireAdmin();
        Tag tag = tagService.getTagById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "标签不存在"));
        validateTagName(request.getName(), tag.getName());
        apply(tag, request);
        return tagService.updateTag(tag);
    }

    @Transactional
    public void deleteTag(Integer id) {
        currentUserService.requireAdmin();
        if (!tagService.deleteTag(id)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "标签不存在或删除失败");
        }
    }

    private void validateCategoryName(String name, String currentName) {
        if (!name.equals(currentName) && categoryService.existsByName(name)) {
            throw new BusinessException(HttpStatus.CONFLICT, "分类名称已存在");
        }
    }

    private void validateParent(Integer parentId, Integer currentId) {
        if (parentId == null) {
            return;
        }
        if (parentId.equals(currentId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "分类不能以自身作为父分类");
        }
        if (!categoryService.getCategoryById(parentId).isPresent()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "父分类不存在");
        }
    }

    private void validateTagName(String name, String currentName) {
        if (!name.equals(currentName) && tagService.existsByName(name)) {
            throw new BusinessException(HttpStatus.CONFLICT, "标签名称已存在");
        }
    }

    private void apply(Category category, CategoryWriteRequest request) {
        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());
        category.setParentId(request.getParentId());
        category.setSortOrder(request.getSortOrder());
    }

    private void apply(Tag tag, TagWriteRequest request) {
        tag.setName(request.getName().trim());
        tag.setDescription(request.getDescription());
        tag.setColor(request.getColor() == null ? "#007bff" : request.getColor());
    }
}
