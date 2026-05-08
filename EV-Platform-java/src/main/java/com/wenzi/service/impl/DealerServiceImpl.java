package com.wenzi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenzi.entity.Dealer;
import com.wenzi.mapper.DealerMapper;
import com.wenzi.service.IDealerService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class DealerServiceImpl extends ServiceImpl<DealerMapper, Dealer> implements IDealerService {

    @Override
    public List<Dealer> getNearestDealers(Double lng, Double lat, String brand) {
        // 现在返回所有符合条件的门店，按距离排序
        return ((DealerMapper) this.baseMapper).findNearestDealers(lng, lat, brand);
    }

    @Override
    public IPage<Dealer> pageQuery(Page<Dealer> page, String keyword) {
        LambdaQueryWrapper<Dealer> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Dealer::getName, keyword);
        }
        wrapper.orderByAsc(Dealer::getId);
        return this.baseMapper.selectPage(page, wrapper);
    }
}