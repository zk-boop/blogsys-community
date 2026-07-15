package com.zk.projectboot.dto;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class UpdateProfileRequest {

    @Size(max = 50)
    private String nickname;

    @Size(max = 500)
    private String avatar;

    @Size(max = 2000)
    private String bio;
}
