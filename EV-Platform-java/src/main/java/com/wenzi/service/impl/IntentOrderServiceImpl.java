package com.wenzi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wenzi.dto.IntentQueryDTO;
import com.wenzi.dto.IntentStatsDTO;
import com.wenzi.dto.IntentSubmitDTO;
import com.wenzi.entity.IntentOrder;
import com.wenzi.entity.Vehicle;
import com.wenzi.mapper.IntentOrderMapper;
import com.wenzi.service.IIntentOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenzi.service.IVehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 购车意向订单表 服务实现类
 * </p>
 *
 * @author 闻志博
 * @since 2026-03-21
 */
@Service
public class IntentOrderServiceImpl extends ServiceImpl<IntentOrderMapper, IntentOrder> implements IIntentOrderService {

    private static final Logger logger = LoggerFactory.getLogger(IntentOrderServiceImpl.class);

    // 🚀 0. 注入车辆服务，用来查品牌和车型
    @Autowired
    private IVehicleService vehicleService;

    @Override
    public void submitIntent(IntentSubmitDTO dto, Long userId) {
        IntentOrder order = new IntentOrder();

        // 1. 填入核心外键和手机号
        order.setUserId(userId);
        order.setVehicleId(dto.getVehicleId());
        order.setContactPhone(dto.getPhone());

        // 2. 核心魔法：将前端的离散字段拼装成 message 存入数据库
        String typeStr = "price".equals(dto.getIntentType()) ? "询问底价" : "预约试驾";
        String formattedMessage = String.format("【%s】城市：%s，称呼：%s", typeStr, dto.getCity(), dto.getName());
        order.setMessage(formattedMessage);

        // 3. 设置初始状态：0-待跟进 (MyBatis Plus 和数据库可能有默认值，这里显式设置更稳妥)
        order.setStatus((byte)0);

        // 4. 落库保存
        this.save(order);
    }

    @Override
    public Page<IntentOrder> pageQuery(IntentQueryDTO dto) {
        // 1. 构建分页对象
        Page<IntentOrder> page = new Page<>(dto.getCurrent(), dto.getSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<IntentOrder> wrapper = new LambdaQueryWrapper<>();

        // 3. 动态搜索
        if (org.apache.commons.lang3.StringUtils.isNotBlank(dto.getKeyword())) {
            String kw = dto.getKeyword();
            wrapper.and(w -> w.like(IntentOrder::getContactPhone, kw)
                    .or()
                    .like(IntentOrder::getMessage, kw));
        }

        // 添加用户ID过滤
        if (dto.getUserId() != null) {
            wrapper.eq(IntentOrder::getUserId, dto.getUserId());
        }

        // 4. 排序逻辑
        wrapper.orderByDesc(IntentOrder::getCreateTime);

        // 5. 执行查询拿到初步结果
        Page<IntentOrder> pageResult = this.page(page, wrapper);

        // 🚀 6. 核心魔法：遍历查询结果，拿着 vehicleId 去车辆表里查名字！
        for (IntentOrder order : pageResult.getRecords()) {
            if (order.getVehicleId() != null) {
                Vehicle v = vehicleService.getById(order.getVehicleId());
                if (v != null) {
                    order.setBrand(v.getBrand());
                    order.setModel(v.getModel());
                } else {
                    order.setBrand("已删除/下架");
                    order.setModel("-");
                }
            }
        }

        return pageResult;
    }

    @Override
    public IntentStatsDTO getIntentStatistics() {
        // 1. 获取所有意向订单
        List<IntentOrder> allIntentOrders = this.list();

        // 2. 获取所有车辆信息，构建Map方便查询
        Map<Long, Vehicle> vehicleMap = vehicleService.list().stream()
                .collect(Collectors.toMap(Vehicle::getId, Function.identity()));

        // 3. 填充 IntentOrder 的品牌和车型信息
        List<IntentOrder> processedOrders = allIntentOrders.stream()
                .peek(order -> {
                    if (order.getVehicleId() != null && vehicleMap.containsKey(order.getVehicleId())) {
                        Vehicle vehicle = vehicleMap.get(order.getVehicleId());
                        order.setBrand(vehicle.getBrand());
                        order.setModel(vehicle.getModel());
                    } else {
                        order.setBrand("未知品牌"); // 对于找不到车辆的意向订单，设置默认值
                        order.setModel("未知车型");
                    }
                })
                .collect(Collectors.toList());

        logger.debug("<<<<DEBUG_PROCESSED_ORDERS>>>> Size of processedOrders: {}", processedOrders.size());
        processedOrders.stream().limit(5).forEach(order -> logger.debug("<<<<DEBUG_PROCESSED_ORDERS>>>> Order Status: {}", order.getStatus()));

        // 4. 统计按品牌分类的数据
        List<IntentStatsDTO.BrandStats> brandStatsList = processedOrders.stream()
                .filter(order -> order.getBrand() != null && !order.getBrand().equals("未知品牌"))
                .collect(Collectors.groupingBy(IntentOrder::getBrand, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> {
                    IntentStatsDTO.BrandStats brandStats = new IntentStatsDTO.BrandStats();
                    brandStats.setBrand(entry.getKey());
                    brandStats.setCount(entry.getValue());
                    return brandStats;
                })
                .collect(Collectors.toList());

        // 5. 统计按车型分类的数据
        List<IntentStatsDTO.ModelStats> modelStatsList = processedOrders.stream()
                .filter(order -> order.getModel() != null && !order.getModel().equals("未知车型"))
                .collect(Collectors.groupingBy(IntentOrder::getModel, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> {
                    IntentStatsDTO.ModelStats modelStats = new IntentStatsDTO.ModelStats();
                    modelStats.setModel(entry.getKey());
                    modelStats.setCount(entry.getValue());
                    return modelStats;
                })
                .collect(Collectors.toList());

        // 6. 统计按状态分类的数据
        List<IntentStatsDTO.StatusStats> statusStatsList = processedOrders.stream()
                .filter(order -> order.getStatus() != null)
                .collect(Collectors.groupingBy(IntentOrder::getStatus, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> {
                    IntentStatsDTO.StatusStats statusStats = new IntentStatsDTO.StatusStats();
                    statusStats.setStatus(entry.getKey());
                    statusStats.setStatusText(convertIntentStatusToText(entry.getKey())); // 转换状态码为文本
                    logger.debug("<<<<DEBUG_COUNT>>>> Status: {}, Count: {}", entry.getKey(), entry.getValue()); // Debug log
                    statusStats.setCount(entry.getValue());
                    return statusStats;
                })
                .collect(Collectors.toList());

        // 7. 构建并返回 IntentStatsDTO
        IntentStatsDTO statsDTO = new IntentStatsDTO();
        statsDTO.setBrandStats(brandStatsList);
        statsDTO.setModelStats(modelStatsList);
        statsDTO.setStatusStats(statusStatsList);
        return statsDTO;
    }

    /**
     * 辅助方法：将意向订单状态码转换为文本描述
     * 跟进状态: 0-待跟进, 1-跟进中, 2-已成交, 3-已取消
     */
    private String convertIntentStatusToText(Byte status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0: return "待跟进";
            case 1: return "跟进中";
            case 2: return "已成交";
            case 3: return "已取消";
            default: return "未知";
        }
    }

}
