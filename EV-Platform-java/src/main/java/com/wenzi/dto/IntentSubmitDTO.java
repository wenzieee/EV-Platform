package com.wenzi.dto;

import lombok.Data;

import java.util.List;

@Data
public class IntentSubmitDTO {
    private Long vehicleId; // 注意：原来 sql 里是 bigint，这里用 Long
    private Long trimId; // 新增：意向车型配置ID
    private String trimName; // 新增：意向车型配置名称
    private String intentType; // price 或 testdrive
    private String city;
    private String name;
    private String phone;
    private List<Long> dealerIds; // 新增：意向4S店ID列表
}