package com.wenzi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime; // 修改导入

@Data
@TableName("biz_post_collection")
public class PostCollection {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private Long userId;

    // 统一为 LocalDateTime
    private LocalDateTime createTime;
}