package com.wenzi.dto;

import com.wenzi.entity.VehicleTrim;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 车辆更新DTO
 */
@Getter
@Setter
public class VehicleUpdateDTO {

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

    private List<VehicleTrim> trims;
}