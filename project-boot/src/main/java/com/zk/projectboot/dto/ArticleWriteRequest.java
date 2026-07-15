package com.zk.projectboot.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Data
public class ArticleWriteRequest {

    @NotBlank
    @Size(max = 255)
    private String title;

    @Size(max = 255)
    private String slug;

    @NotBlank
    private String content;

    @Size(max = 2000)
    private String excerpt;

    @Size(max = 500)
    private String coverImage;

    private List<Integer> tagIds = new ArrayList<>();
}
