package com.zk.projectboot.dto;

import com.zk.projectboot.model.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类数据传输对象
 * 用于在API接口中传输分类数据，避免直接暴露实体类
 * 提供了分类的基本信息、层级关系和文章数量等信息
 */
@Data                // Lombok注解，自动生成getter、setter、equals、hashCode和toString方法
@NoArgsConstructor   // Lombok注解，生成无参构造函数
@AllArgsConstructor  // Lombok注解，生成包含所有字段的构造函数
public class CategoryDTO {

    /**
     * 分类ID
     */
    private Integer id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 父分类ID
     * 如果为null，则表示这是一个顶级分类
     */
    private Integer parentId;

    /**
     * 父分类名称
     * 方便前端直接显示父分类信息，无需再次查询
     */
    private String parentName;

    /**
     * 分类排序顺序
     * 数值越小排序越靠前
     */
    private Integer sortOrder;

    /**
     * 分类状态
     * '1'表示已批准可正常使用
     * 'PENDING'或'0'表示待审核状态
     * '2'表示已拒绝状态
     */
    private String status;

    /**
     * 子分类列表
     * 用于构建分类树形结构
     */
    private List<CategoryDTO> children = new ArrayList<>();

    /**
     * 该分类下的文章数量
     * 用于前端显示统计信息
     */
    private Integer articleCount;

    /**
     * 将Category实体转换为CategoryDTO对象
     * 包含递归转换子分类
     *
     * @param category 分类实体对象
     * @return 转换后的CategoryDTO对象，如果输入为null则返回null
     */
    public static CategoryDTO fromCategory(Category category) {
        if (category == null) {
            return null;
        }

        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(category.getId());
        categoryDTO.setName(category.getName());
        categoryDTO.setDescription(category.getDescription());
        categoryDTO.setParentId(category.getParentId());
        categoryDTO.setStatus(category.getStatus()); // 添加状态转换

        // 设置父分类名称，如果存在父分类
        if (category.getParent() != null) {
            categoryDTO.setParentName(category.getParent().getName());
        }

        categoryDTO.setSortOrder(category.getSortOrder());

        // 计算分类下的文章数量
        if (category.getArticles() != null) {
            categoryDTO.setArticleCount(category.getArticles().size());
        } else {
            categoryDTO.setArticleCount(0);
        }

        // 递归转换子分类
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            List<CategoryDTO> childrenDTO = category.getChildren().stream()
                    .map(CategoryDTO::fromCategory)
                    .collect(Collectors.toList());
            categoryDTO.setChildren(childrenDTO);
        }

        return categoryDTO;
    }
}
