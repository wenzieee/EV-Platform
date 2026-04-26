package com.wenzi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 评论点赞DTO
 */
@Data
public class CommentLikeDTO {
    
    @NotNull(message = "评论ID不能为空")
    private Long commentId;
    
    @NotNull(message = "点赞状态不能为空")
    private Boolean isLiked;
}
