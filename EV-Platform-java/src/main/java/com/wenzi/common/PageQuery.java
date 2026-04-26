package com.wenzi.common;

import lombok.Data;

/**
 * 分页查询基类DTO
 * 所有需要分页的查询DTO都继承这个类
 */
@Data
public class PageQuery {
    /**
     * 当前页码
     */
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;
}