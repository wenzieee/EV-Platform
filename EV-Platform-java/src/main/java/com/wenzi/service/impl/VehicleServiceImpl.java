package com.wenzi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wenzi.dto.VehicleQueryDTO;
import com.wenzi.entity.Vehicle;
import com.wenzi.mapper.VehicleMapper;
import com.wenzi.service.IVehicleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public Page<Vehicle> pageQuery(VehicleQueryDTO dto) {
        // 1. 构建分页对象
        Page<Vehicle> page = new Page<>(dto.getCurrent(), dto.getSize());

        // 2. 构建条件查询构造器
        LambdaQueryWrapper<Vehicle> wrapper = new LambdaQueryWrapper<>();

        // 3. 基础查询条件 (品牌、价格下限、价格上限)
        wrapper.eq(StringUtils.isNotBlank(dto.getBrand()), Vehicle::getBrand, dto.getBrand())
                .ge(dto.getMinPrice() != null, Vehicle::getPrice, dto.getMinPrice())
                .le(dto.getMaxPrice() != null, Vehicle::getPrice, dto.getMaxPrice());

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
        return this.page(page, wrapper);
    }
    @Override
    public List<Vehicle> getHotVehicles() {
        LambdaQueryWrapper<Vehicle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Vehicle::getStatus, 1)          // 必须是上架状态
                .orderByDesc(Vehicle::getHotScore)  // 核心：按热度值降序排列
                .last("LIMIT 6");                   // 只取前 6 条
        return this.list(wrapper);
    }

}
