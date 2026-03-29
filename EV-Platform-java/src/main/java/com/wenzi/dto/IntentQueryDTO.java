package com.wenzi.dto;

import lombok.Data;

@Data
public class IntentQueryDTO {
    private Integer current = 1; // 当前页码
    private Integer size = 10;   // 每页条数
    private String keyword;      // 搜索关键字 (姓名或手机号)
    private Long userId;         // 用户ID，用于查询个人预约记录
}