package com.wenzi.dto;

import com.wenzi.entity.VehicleTrim;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 车辆创建DTO
 */
@Getter
@Setter
public class VehicleCreateDTO {

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

    private List<VehicleTrim> trims;
}