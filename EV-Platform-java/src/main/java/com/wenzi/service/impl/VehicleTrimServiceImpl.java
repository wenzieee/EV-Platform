package com.wenzi.service.impl;

import com.wenzi.entity.VehicleTrim;
import com.wenzi.mapper.VehicleTrimMapper;
import com.wenzi.service.IVehicleTrimService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 车型配置表（SKU） 服务实现类
 * </p>
 *
 * @author 闻志博
 * @since 2026-05-05
 */
@Service
public class VehicleTrimServiceImpl extends ServiceImpl<VehicleTrimMapper, VehicleTrim> implements IVehicleTrimService {

    @Override
    public List<VehicleTrim> getTrimsByVehicleId(Long vehicleId) {
        LambdaQueryWrapper<VehicleTrim> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VehicleTrim::getVehicleId, vehicleId)
               .eq(VehicleTrim::getIsDeleted, 0)
               .orderByDesc(VehicleTrim::getIsHot)
               .orderByAsc(VehicleTrim::getPrice);
        return this.list(wrapper);
    }

    @Override
    @Transactional
    public boolean saveTrims(Long vehicleId, List<VehicleTrim> trims) {
        // 先删除旧的配置
        this.removeByVehicleId(vehicleId);
        
        // 保存新配置
        LocalDateTime now = LocalDateTime.now();
        for (VehicleTrim trim : trims) {
            trim.setVehicleId(vehicleId);
            trim.setIsDeleted((byte) 0);
            trim.setCreateTime(now);
            trim.setUpdateTime(now);
            if (trim.getStatus() == null) {
                trim.setStatus((byte) 1);
            }
            if (trim.getIsHot() == null) {
                trim.setIsHot((byte) 0);
            }
        }
        return this.saveBatch(trims);
    }

    @Override
    @Transactional
    public boolean removeByVehicleId(Long vehicleId) {
        LambdaQueryWrapper<VehicleTrim> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VehicleTrim::getVehicleId, vehicleId);
        return this.remove(wrapper);
    }
}