package com.wenzi.vo;

import com.wenzi.entity.VehicleTrim;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 车辆详情VO（包含配置列表）
 */
@Getter
@Setter
public class VehicleDetailVO {

    private Long id;

    private String brand;

    private String model;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private Integer rangeKm;

    private String driveType;

    private String imageUrl;

    private String configuration;

    private Byte status;

    private Integer hotScore;

    /**
     * 车型配置列表（SKU）
     */
    private List<VehicleTrim> trims;
}