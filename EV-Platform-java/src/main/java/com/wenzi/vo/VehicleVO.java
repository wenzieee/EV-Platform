package com.wenzi.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 车辆VO（用于列表展示）
 */
@Getter
@Setter
public class VehicleVO {

    private Long id;

    private String brand;

    private String model;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private Integer rangeKm;

    private String driveType;

    private String imageUrl;

    private Byte status;

    private Integer hotScore;
}