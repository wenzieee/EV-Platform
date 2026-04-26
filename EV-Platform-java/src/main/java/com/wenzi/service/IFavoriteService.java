package com.wenzi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wenzi.entity.Favorite;

public interface IFavoriteService extends IService<Favorite> {
    // 切换收藏状态（如果没有收藏则加入，如果已经收藏则取消）
    boolean toggleFavorite(Long userId, Long vehicleId);
}