package com.wenzi.dto;

import lombok.Data;

@Data
public class UserQueryDTO {
    private Integer current = 1;
    private Integer size = 10;
    private String keyword; // 搜索用户名
}