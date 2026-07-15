package com.zk.projectboot.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class CommentWriteRequest {

    @NotBlank
    @Size(max = 5000)
    private String content;
}
