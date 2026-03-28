package com.wenzi.dto;

import lombok.Data;

/**
 * 车辆条件查询参数封装
 */
@Data
public class VehicleQueryDTO {
    // 分页基础参数（给定默认值，防止前端没传报错）
    private Integer current = 1; // 当前页码
    private Integer size = 10;   // 每页显示条数

    // 业务筛选参数
    private String brand;        // 品牌 (精确匹配)
    private String keyword;      // 搜索关键字，如车型 (模糊匹配)
    private Double minPrice;     // 最低价
    private Double maxPrice;     // 最高价
    private Boolean isAdmin;
}