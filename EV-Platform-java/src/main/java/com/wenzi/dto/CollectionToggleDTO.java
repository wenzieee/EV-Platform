package com.wenzi.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class CollectionToggleDTO {

    @NotNull(message = "帖子ID不能为空")
    private Long postId;

    @NotNull(message = "收藏状态不能为空")
    private Boolean isCollected; // true为收藏，false为取消收藏
}
