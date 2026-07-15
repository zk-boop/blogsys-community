package com.zk.projectboot.dto;

import com.zk.projectboot.model.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagDTO {

    private Integer id;
    private String name;
    private String description;
    private String color;
    /**
     * 标签状态
     * 1表示已批准可正常使用
     * 0表示待审核状态
     * 2表示已拒绝状态
     */
    private Integer status;
    private Integer articleCount;

    public static TagDTO fromTag(Tag tag) {
        if (tag == null) {
            return null;
        }

        TagDTO tagDTO = new TagDTO();
        tagDTO.setId(tag.getId());
        tagDTO.setName(tag.getName());
        tagDTO.setDescription(tag.getDescription());
        tagDTO.setColor(tag.getColor());
        tagDTO.setStatus(tag.getStatus()); // 添加状态转换

        if (tag.getArticleTags() != null) {
            tagDTO.setArticleCount(tag.getArticleTags().size());
        } else {
            tagDTO.setArticleCount(0);
        }

        return tagDTO;
    }
}
