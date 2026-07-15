package com.zk.projectboot.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class CategoryWriteRequest {
    @NotBlank
    @Size(max = 100)
    private String name;
    @Size(max = 500)
    private String description;
    private Integer parentId;
    private Integer sortOrder;
}
