package com.wenzi.dto;

import com.wenzi.entity.Vehicle;
import com.wenzi.entity.VehicleTrim;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 车辆详情响应 DTO
 */
@Getter
@Setter
public class VehicleDetailDTO {

    /**
     * 车辆基本信息
     */
    private Vehicle vehicle;

    /**
     * 车型配置列表（SKU）
     */
    private List<VehicleTrim> trims;

    public VehicleDetailDTO() {}

    public VehicleDetailDTO(Vehicle vehicle, List<VehicleTrim> trims) {
        this.vehicle = vehicle;
        this.trims = trims;
    }
}