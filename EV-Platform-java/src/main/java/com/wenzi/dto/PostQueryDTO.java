package com.wenzi.dto;

import com.wenzi.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PostQueryDTO extends PageQuery {
    private Long userId;
    private String title;
    private Integer status; // 帖子状态
}
