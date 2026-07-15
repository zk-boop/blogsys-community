package com.zk.projectboot.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class TagWriteRequest {
    @NotBlank
    @Size(max = 100)
    private String name;
    @Size(max = 500)
    private String description;
    @Pattern(regexp = "^#[0-9a-fA-F]{6}$")
    private String color;
}
