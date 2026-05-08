package com.wenzi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wenzi.entity.Dealer;
import java.util.List;

public interface IDealerService extends IService<Dealer> {
    // 寻找最近的4S店
    List<Dealer> getNearestDealers(Double lng, Double lat, String brand);
    
    // 分页查询门店列表（支持关键词搜索）
    IPage<Dealer> pageQuery(Page<Dealer> page, String keyword);
}