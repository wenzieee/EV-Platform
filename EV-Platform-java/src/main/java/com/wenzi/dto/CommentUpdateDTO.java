package com.wenzi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
public class CommentUpdateDTO {

    @NotNull(message = "评论ID不能为空")
    private Long id;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过500字")
    private String content;

    private Integer status; // 状态: 1正常, 0删除/隐藏
}
