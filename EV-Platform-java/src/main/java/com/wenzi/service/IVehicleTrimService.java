package com.wenzi.service;

import com.wenzi.entity.VehicleTrim;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 车型配置表（SKU） 服务类
 * </p>
 *
 * @author 闻志博
 * @since 2026-05-05
 */
public interface IVehicleTrimService extends IService<VehicleTrim> {

    /**
     * 根据车辆ID获取所有配置
     */
    List<VehicleTrim> getTrimsByVehicleId(Long vehicleId);

    /**
     * 保存车辆配置（批量）
     */
    boolean saveTrims(Long vehicleId, List<VehicleTrim> trims);

    /**
     * 删除车辆的所有配置
     */
    boolean removeByVehicleId(Long vehicleId);
}