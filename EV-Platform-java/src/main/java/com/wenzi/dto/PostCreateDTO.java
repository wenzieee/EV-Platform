package com.wenzi.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
public class PostCreateDTO {

    @NotBlank(message = "帖子内容不能为空")
    @Size(max = 65535, message = "帖子内容过长") // TEXT类型最大长度
    private String content;

    @Size(max = 100, message = "帖子标题不能超过100字")
    private String title;

    @NotNull(message = "媒体类型不能为空")
    private Integer mediaType; // 媒体类型: 0纯文本, 1图文, 2视频

    private List<String> mediaUrls; // 图片或视频的URL数组
}
