package com.wenzi.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
public class PostUpdateDTO {

    @NotNull(message = "帖子ID不能为空")
    private Long id;

    @Size(max = 65535, message = "帖子内容过长")
    private String content;

    @Size(max = 100, message = "帖子标题不能超过100字")
    private String title;

    private Integer mediaType; // 媒体类型: 0纯文本, 1图文, 2视频

    private List<String> mediaUrls; // 图片或视频的URL数组

    private Integer status; // 状态: 1正常展示, 0用户删除或后台违规隐藏
}
