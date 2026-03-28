package com.wenzi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wenzi.dto.IntentQueryDTO;
import com.wenzi.dto.IntentSubmitDTO;
import com.wenzi.entity.IntentOrder;
import com.wenzi.entity.Vehicle;
import com.wenzi.mapper.IntentOrderMapper;
import com.wenzi.service.IIntentOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenzi.service.IVehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

}
