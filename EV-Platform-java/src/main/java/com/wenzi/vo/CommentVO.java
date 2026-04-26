package com.wenzi.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import com.wenzi.entity.Comment;

import java.time.LocalDateTime;

@Data
public class CommentVO extends Comment { // 继承 BizComment，方便获取原有字段
    private String username; // 评论者用户名
    private String nickname;
    private String avatar;   // 评论者头像

    private String parentUsername; // 如果是回复，父评论者的用户名
    private Boolean liked; // 当前用户是否已点赞该评论
    private String postTitle; // 评论所属帖子的标题

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
