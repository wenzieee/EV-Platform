package com.wenzi.dto;

import com.wenzi.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class CommentQueryDTO extends PageQuery {
    @NotNull(message = "所属帖子ID不能为空")
    private Long postId;

    private Long userId; // 评论者ID
    private Long parentId; // 查询某个父评论下的回复 (如果是0，则查询所有直接评论)
    private Integer status; // 评论状态
}
