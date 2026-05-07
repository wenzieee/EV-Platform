package com.wenzi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wenzi.dto.VehicleQueryDTO;
import com.wenzi.dto.VehicleStatsDTO;
import com.wenzi.dto.VehicleCreateDTO;
import com.wenzi.dto.VehicleUpdateDTO;
import com.wenzi.vo.VehicleDetailVO;
import com.wenzi.vo.VehicleVO;
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

    /**
     * 兼容旧版的分页查询方法
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<Vehicle> pageQuery(VehicleQueryDTO queryDTO);

    /**
     * 多条件分页查询车辆 (C端和管理后台通用)
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<VehicleVO> pageQueryVehicles(VehicleQueryDTO queryDTO);

    /**
     * 获取车辆详情，包含所有车型配置
     * @param id 车辆ID
     * @return 车辆详情VO
     */
    VehicleDetailVO getVehicleDetail(Long id);

    /**
     * 创建车辆 (管理后台)
     * @param createDTO 车辆创建DTO
     * @return 是否成功
     */
    boolean createVehicle(VehicleCreateDTO createDTO);

    /**
     * 更新车辆 (管理后台)
     * @param updateDTO 车辆更新DTO
     * @return 是否成功
     */
    boolean updateVehicle(VehicleUpdateDTO updateDTO);

    /**
     * 删除车辆 (管理后台，逻辑删除)
     * @param id 车辆ID
     * @return 是否成功
     */
    boolean deleteVehicle(Long id);

    /**
     * 获取热门推荐车辆 (前6名)
     * @return 车辆列表
     */
    List<Vehicle> getHotVehicles();

    /**
     * 获取车辆统计数据
     * @return 统计数据DTO
     */
    VehicleStatsDTO getVehicleStatistics();
}
