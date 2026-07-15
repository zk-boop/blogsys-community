package com.zk.projectboot.controller;

import com.zk.projectboot.application.TaxonomyApplicationService;
import com.zk.projectboot.dto.ApiResponse;
import com.zk.projectboot.dto.CategoryDTO;
import com.zk.projectboot.dto.CategoryWriteRequest;
import com.zk.projectboot.exception.BusinessException;
import com.zk.projectboot.model.Category;
import com.zk.projectboot.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final TaxonomyApplicationService taxonomyService;

    public CategoryController(CategoryService categoryService,
                              TaxonomyApplicationService taxonomyService) {
        this.categoryService = categoryService;
        this.taxonomyService = taxonomyService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> getById(@PathVariable Integer id) {
        Category category = categoryService.getCategoryById(id)
                .filter(value -> "1".equals(value.getStatus()))
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "分类不存在"));
        return ResponseEntity.ok(ApiResponse.success(CategoryDTO.fromCategory(category)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(toDtos(categoryService.getAllApprovedCategories())));
    }

    @GetMapping("/root")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getRoots() {
        return ResponseEntity.ok(ApiResponse.success(toDtos(categoryService.getApprovedRootCategories())));
    }

    @GetMapping("/subcategories/{parentId}")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getChildren(@PathVariable Integer parentId) {
        return ResponseEntity.ok(ApiResponse.success(
                toDtos(categoryService.getApprovedSubcategoriesByParentId(parentId))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryDTO>> create(
            @Valid @RequestBody CategoryWriteRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                CategoryDTO.fromCategory(taxonomyService.submitCategory(request))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> update(
            @PathVariable Integer id,
            @Valid @RequestBody CategoryWriteRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                CategoryDTO.fromCategory(taxonomyService.updateCategory(id, request))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        taxonomyService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("分类已删除", null));
    }

    private List<CategoryDTO> toDtos(List<Category> categories) {
        return categories.stream().map(CategoryDTO::fromCategory).collect(Collectors.toList());
    }
}
