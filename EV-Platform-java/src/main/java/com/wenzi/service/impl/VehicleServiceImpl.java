package com.wenzi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenzi.dto.VehicleCreateDTO;
import com.wenzi.dto.VehicleQueryDTO;
import com.wenzi.dto.VehicleStatsDTO;
import com.wenzi.dto.VehicleUpdateDTO;
import com.wenzi.entity.Vehicle;
import com.wenzi.entity.VehicleTrim;
import com.wenzi.mapper.VehicleMapper;
import com.wenzi.service.IVehicleService;
import com.wenzi.service.IVehicleTrimService;
import com.wenzi.vo.VehicleDetailVO;
import com.wenzi.vo.VehicleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 新能源汽车信息表 服务实现类
 * </p>
 *
 * @author 闻志博
 * @since 2026-03-21
 */
@Service
public class VehicleServiceImpl extends ServiceImpl<VehicleMapper, Vehicle> implements IVehicleService {

    @Autowired
    private IVehicleTrimService vehicleTrimService;

    /**
     * 兼容旧版接口的分页查询方法
     */
    public Page<Vehicle> pageQuery(VehicleQueryDTO dto) {
        Page<Vehicle> page = new Page<>(dto.getCurrent(), dto.getSize());
        LambdaQueryWrapper<Vehicle> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(StringUtils.isNotBlank(dto.getBrand()), Vehicle::getBrand, dto.getBrand())
               .eq(StringUtils.isNotBlank(dto.getDriveType()), Vehicle::getDriveType, dto.getDriveType());
        
        // 价格区间筛选逻辑
        if (dto.getMinPrice() != null && dto.getMaxPrice() != null) {
            double min = dto.getMinPrice();
            double max = dto.getMaxPrice();
            
            if (min == 0 && max == 15) {
                // 15万以下：最低价格 < 15万
                wrapper.lt(Vehicle::getMinPrice, max);
            } else if (max == 999) {
                // 35万以上：最低价格 > 35万
                wrapper.gt(Vehicle::getMinPrice, min);
            } else {
                // 中间区间：最低价格在 [min, max] 之间
                wrapper.between(Vehicle::getMinPrice, min, max);
            }
        }

        if (StringUtils.isNotBlank(dto.getKeyword())) {
            String kw = dto.getKeyword();
            wrapper.and(w -> w
                    .like(Vehicle::getBrand, kw)
                    .or()
                    .like(Vehicle::getModel, kw)
                    .or()
                    .apply("CONCAT(brand, ' ', model) LIKE {0}", "%" + kw + "%")
                    .or()
                    .apply("CONCAT(brand, model) LIKE {0}", "%" + kw + "%")
            );
        }

        if (dto.getIsAdmin() != null && dto.getIsAdmin()) {
            wrapper.orderByAsc(Vehicle::getId);
        } else {
            wrapper.eq(Vehicle::getStatus, 1)
                   .orderByDesc(Vehicle::getUpdateTime);
        }

        return this.page(page, wrapper);
    }

    @Override
    public IPage<VehicleVO> pageQueryVehicles(VehicleQueryDTO dto) {
        // 1. 构建分页对象
        IPage<Vehicle> page = new Page<>(dto.getCurrent(), dto.getSize());

        // 2. 构建条件查询构造器
        LambdaQueryWrapper<Vehicle> wrapper = new LambdaQueryWrapper<>();

        // 3. 基础查询条件 (品牌、驱动类型)
        wrapper.eq(StringUtils.isNotBlank(dto.getBrand()), Vehicle::getBrand, dto.getBrand())
                .eq(StringUtils.isNotBlank(dto.getDriveType()), Vehicle::getDriveType, dto.getDriveType());
        
        // 4. 价格区间筛选逻辑
        if (dto.getMinPrice() != null && dto.getMaxPrice() != null) {
            double min = dto.getMinPrice();
            double max = dto.getMaxPrice();
            
            if (min == 0 && max == 15) {
                // 15万以下：最低价格 < 15万
                wrapper.lt(Vehicle::getMinPrice, max);
            } else if (max == 999) {
                // 35万以上：最低价格 > 35万
                wrapper.gt(Vehicle::getMinPrice, min);
            } else {
                // 中间区间：最低价格在 [min, max] 之间
                wrapper.between(Vehicle::getMinPrice, min, max);
            }
        }

        // 🚀 核心升级：处理全局搜索关键词 (跨字段模糊匹配)
        if (StringUtils.isNotBlank(dto.getKeyword())) {
            String kw = dto.getKeyword();
            // 必须使用 .and(w -> ...) 把这三个 OR 条件包裹在一个括号里，
            // 否则会破坏前面品牌和价格的 AND 逻辑
            wrapper.and(w -> w
                    // 匹配品牌 (如: 比亚迪)
                    .like(Vehicle::getBrand, kw)
                    .or()
                    // 匹配车型 (如: 海豹)
                    .like(Vehicle::getModel, kw)
                    .or()
                    // 跨字段拼接匹配 (如: 比亚迪海豹)
                    // 注意这里的中间加了一个空格，适应 "比亚迪 海豹" 这样的底层存储结构
                    .apply("CONCAT(brand, ' ', model) LIKE {0}", "%" + kw + "%")
                    .or()
                    // 以防万一，再加一个没有空格的无缝拼接，绝对不会漏掉数据
                    .apply("CONCAT(brand, model) LIKE {0}", "%" + kw + "%")
            );
        }

        // 4. 🚀 动态查询与排序：完美隔离 C端用户 和 B端管理员！
        if (dto.getIsAdmin() != null && dto.getIsAdmin()) {
            // 【后台管理员视角】
            // 不限制上下架状态 (全都能看)
            // 排序：按 ID 从小到大正序排列
            wrapper.orderByAsc(Vehicle::getId);
        } else {
            // 【前台 C端用户视角】
            // 状态：只能看状态为 1 (上架中) 的车辆
            // 排序：按最新发布时间倒序排列 (保证首页轮播图永远是最新的重磅车！)
            wrapper.eq(Vehicle::getStatus, 1)
                    .orderByDesc(Vehicle::getUpdateTime);
        }

        // 5. 执行分页查询并返回
        IPage<Vehicle> vehiclePage = this.page(page, wrapper);

        return vehiclePage.convert(vehicle -> {
            VehicleVO vo = new VehicleVO();
            BeanUtils.copyProperties(vehicle, vo);
            // 对于VO，可以根据需要设置一些额外字段，例如格式化价格显示等
            return vo;
        });
    }

    @Override
    public VehicleDetailVO getVehicleDetail(Long id) {
        Vehicle vehicle = this.getById(id);
        if (vehicle == null || vehicle.getIsDeleted() == 1) {
            return null;
        }

        VehicleDetailVO detailVO = new VehicleDetailVO();
        BeanUtils.copyProperties(vehicle, detailVO);

        // 查询所有关联的车型配置
        LambdaQueryWrapper<VehicleTrim> trimWrapper = new LambdaQueryWrapper<>();
        trimWrapper.eq(VehicleTrim::getVehicleId, id)
                   .eq(VehicleTrim::getIsDeleted, 0) // 只查询未删除的配置
                   .eq(VehicleTrim::getStatus, 1); // 只查询上架状态的配置
        List<VehicleTrim> trims = vehicleTrimService.list(trimWrapper);
        detailVO.setTrims(trims);

        return detailVO;
    }

    @Override
    @Transactional
    public boolean createVehicle(VehicleCreateDTO createDTO) {
        Vehicle vehicle = new Vehicle();
        BeanUtils.copyProperties(createDTO, vehicle);
        vehicle.setCreateTime(LocalDateTime.now());
        vehicle.setUpdateTime(LocalDateTime.now());
        vehicle.setIsDeleted((byte) 0);
        vehicle.setHotScore(0);
        vehicle.setStatus((byte) 1); // 默认上架

        boolean success = this.save(vehicle);
        if (success && createDTO.getTrims() != null && !createDTO.getTrims().isEmpty()) {
            for (VehicleTrim trimDTO : createDTO.getTrims()) {
                VehicleTrim vehicleTrim = new VehicleTrim();
                BeanUtils.copyProperties(trimDTO, vehicleTrim);
                vehicleTrim.setVehicleId(vehicle.getId()); // 关联到主车ID
                vehicleTrim.setCreateTime(LocalDateTime.now());
                vehicleTrim.setUpdateTime(LocalDateTime.now());
                vehicleTrim.setIsDeleted((byte) 0);
                vehicleTrim.setStatus((byte) 1); // 默认上架
                vehicleTrimService.save(vehicleTrim);
            }
        }
        return success;
    }

    @Override
    public List<Vehicle> getHotVehicles() {
        LambdaQueryWrapper<Vehicle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Vehicle::getStatus, 1)          // 必须是上架状态
                .eq(Vehicle::getIsDeleted, 0)      // 必须是未删除状态
                .orderByDesc(Vehicle::getHotScore)  // 核心：按热度值降序排列
                .last("LIMIT 6");                   // 只取前 6 条
        return this.list(wrapper);
    }

    @Override
    public VehicleStatsDTO getVehicleStatistics() {
        // 1. 查询所有上架中的未删除车辆
        LambdaQueryWrapper<Vehicle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Vehicle::getStatus, 1)
                    .eq(Vehicle::getIsDeleted, 0);
        List<Vehicle> onSaleVehicles = this.list(queryWrapper);

        // 2. 统计按品牌分类的数据
        List<VehicleStatsDTO.BrandStats> brandStatsList = onSaleVehicles.stream()
                .collect(Collectors.groupingBy(Vehicle::getBrand, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> {
                    VehicleStatsDTO.BrandStats brandStats = new VehicleStatsDTO.BrandStats();
                    brandStats.setBrand(entry.getKey());
                    brandStats.setCount(entry.getValue());
                    return brandStats;
                })
                .collect(Collectors.toList());

        // 3. 统计按驱动类型分类的数据
        List<VehicleStatsDTO.DriveTypeStats> driveTypeStatsList = onSaleVehicles.stream()
                .collect(Collectors.groupingBy(Vehicle::getDriveType, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> {
                    VehicleStatsDTO.DriveTypeStats driveTypeStats = new VehicleStatsDTO.DriveTypeStats();
                    driveTypeStats.setDriveType(entry.getKey());
                    driveTypeStats.setCount(entry.getValue());
                    return driveTypeStats;
                })
                .collect(Collectors.toList());

        // 4. 构建并返回 VehicleStatsDTO
        VehicleStatsDTO statsDTO = new VehicleStatsDTO();
        statsDTO.setBrandStats(brandStatsList);
        statsDTO.setDriveTypeStats(driveTypeStatsList);
        return statsDTO;
    }

    @Override
    @Transactional
    public boolean updateVehicle(VehicleUpdateDTO updateDTO) {
        Vehicle existingVehicle = this.getById(updateDTO.getId());
        if (existingVehicle == null || existingVehicle.getIsDeleted() == 1) {
            return false; // 车辆不存在或已删除
        }

        Vehicle vehicle = new Vehicle();
        BeanUtils.copyProperties(updateDTO, vehicle);
        vehicle.setUpdateTime(LocalDateTime.now());
        // 不允许通过此接口修改isDeleted，hotScore，createTime

        boolean success = this.updateById(vehicle);

        if (success) {
            // 处理车型配置 (Trims)
            // 1. 获取现有Trims
            LambdaQueryWrapper<VehicleTrim> existingTrimsWrapper = new LambdaQueryWrapper<>();
            existingTrimsWrapper.eq(VehicleTrim::getVehicleId, updateDTO.getId());
            List<VehicleTrim> existingTrims = vehicleTrimService.list(existingTrimsWrapper);

            // 2. 根据传入的Trims更新、新增或删除
            List<VehicleTrim> updatedTrims = updateDTO.getTrims();
            if (updatedTrims != null) {
                // 更新或新增
                for (VehicleTrim trimDTO : updatedTrims) {
                    if (trimDTO.getId() != null) { // 存在ID，则为更新
                        Optional<VehicleTrim> found = existingTrims.stream()
                                .filter(t -> t.getId().equals(trimDTO.getId()))
                                .findFirst();
                        if (found.isPresent()) {
                            BeanUtils.copyProperties(trimDTO, found.get());
                            found.get().setUpdateTime(LocalDateTime.now());
                            vehicleTrimService.updateById(found.get());
                        } else {
                            // ID存在但不在现有列表中，可能是新的或错误ID，这里选择新增
                            VehicleTrim newTrim = new VehicleTrim();
                            BeanUtils.copyProperties(trimDTO, newTrim);
                            newTrim.setVehicleId(updateDTO.getId());
                            newTrim.setCreateTime(LocalDateTime.now());
                            newTrim.setUpdateTime(LocalDateTime.now());
                            newTrim.setIsDeleted((byte) 0);
                            newTrim.setStatus((byte) 1);
                            vehicleTrimService.save(newTrim);
                        }
                    } else { // 无ID，则为新增
                        VehicleTrim newTrim = new VehicleTrim();
                        BeanUtils.copyProperties(trimDTO, newTrim);
                        newTrim.setVehicleId(updateDTO.getId());
                        newTrim.setCreateTime(LocalDateTime.now());
                        newTrim.setUpdateTime(LocalDateTime.now());
                        newTrim.setIsDeleted((byte) 0);
                        newTrim.setStatus((byte) 1);
                        vehicleTrimService.save(newTrim);
                    }
                }

                // 删除不再包含的Trims (逻辑删除)
                List<Long> updatedTrimIds = updatedTrims.stream()
                        .filter(t -> t.getId() != null)
                        .map(VehicleTrim::getId)
                        .collect(Collectors.toList());
                existingTrims.stream()
                        .filter(t -> !updatedTrimIds.contains(t.getId()))
                        .forEach(t -> {
                            t.setIsDeleted((byte) 1);
                            t.setUpdateTime(LocalDateTime.now());
                            vehicleTrimService.updateById(t);
                        });
            } else { // 如果传入的trims为null，表示清空所有Trims
                existingTrims.forEach(t -> {
                    t.setIsDeleted((byte) 1);
                    t.setUpdateTime(LocalDateTime.now());
                    vehicleTrimService.updateById(t);
                });
            }
        }
        return success;
    }

    @Override
    @Transactional
    public boolean deleteVehicle(Long id) {
        Vehicle vehicle = this.getById(id);
        if (vehicle == null || vehicle.getIsDeleted() == 1) {
            return false; // 车辆不存在或已删除
        }

        // 逻辑删除主车
        vehicle.setIsDeleted((byte) 1);
        vehicle.setUpdateTime(LocalDateTime.now());
        boolean success = this.updateById(vehicle);

        // 逻辑删除所有关联的车型配置
        if (success) {
            LambdaQueryWrapper<VehicleTrim> trimWrapper = new LambdaQueryWrapper<>();
            trimWrapper.eq(VehicleTrim::getVehicleId, id);
            List<VehicleTrim> trims = vehicleTrimService.list(trimWrapper);
            trims.forEach(trim -> {
                trim.setIsDeleted((byte) 1);
                trim.setUpdateTime(LocalDateTime.now());
                vehicleTrimService.updateById(trim);
            });
        }
        return success;
    }
}

