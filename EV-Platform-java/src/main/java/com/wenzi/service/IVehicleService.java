package com.wenzi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wenzi.dto.VehicleQueryDTO;
import com.wenzi.dto.VehicleStatsDTO;
import com.wenzi.entity.Vehicle;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 新能源汽车信息表 服务类
 * </p>
 *
 * @author 闻志博
 * @since 2026-03-21
 */
public interface IVehicleService extends IService<Vehicle> {

    // 新增：多条件分页查询
    Page<Vehicle> pageQuery(VehicleQueryDTO queryDTO);

    // 获取热门推荐车辆 (前6名)
    List<Vehicle> getHotVehicles();

    // 获取车辆统计数据
    VehicleStatsDTO getVehicleStatistics();

}
