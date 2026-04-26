package com.wenzi.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class LikeToggleDTO {

    @NotNull(message = "帖子ID不能为空")
    private Long postId;

    @NotNull(message = "点赞状态不能为空")
    private Boolean isLiked; // true为点赞，false为取消点赞
}
