package com.wenzi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wenzi.dto.IntentQueryDTO;
import com.wenzi.dto.IntentSubmitDTO;
import com.wenzi.dto.IntentStatsDTO;
import com.wenzi.entity.IntentOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 购车意向订单表 服务类
 * </p>
 *
 * @author 闻志博
 * @since 2026-03-21
 */
public interface IIntentOrderService extends IService<IntentOrder> {

    void submitIntent(IntentSubmitDTO dto, Long userId);

    // 🚀 新增：多条件分页查询用户线索
    Page<IntentOrder> pageQuery(IntentQueryDTO dto);

    // 🚀 新增：获取意向订单统计数据
    IntentStatsDTO getIntentStatistics();
}
