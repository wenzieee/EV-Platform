package com.wenzi.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
public class CommentCreateDTO {

    @NotNull(message = "所属帖子ID不能为空")
    private Long postId;

    @NotNull(message = "父评论ID不能为空")
    private Long parentId; // 父评论ID (0表示是对帖子的直接评论，非0表示回复别人的评论)

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过500字")
    private String content;
}
