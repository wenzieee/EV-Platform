package com.wenzi.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.wenzi.entity.Post;

import java.util.Date; // ⚠️ 注意：引入 java.util.Date
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class PostVO extends Post {
    private String username;
    private String nickname;
    private String avatar;

    private Boolean liked;
    private Boolean collected;
    private Boolean isFollowed;

    // 用这个字段来传给前端数组
    private List<String> mediaUrlList;

    // ⚠️ 统一改为 Date 类型，并加上时区防止前端时间差8小时
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}