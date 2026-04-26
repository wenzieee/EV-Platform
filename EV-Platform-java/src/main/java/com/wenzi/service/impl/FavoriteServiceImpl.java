package com.wenzi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenzi.entity.Favorite;
import com.wenzi.mapper.FavoriteMapper;
import com.wenzi.service.IFavoriteService;
import org.springframework.stereotype.Service;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements IFavoriteService {

    @Override
    public boolean toggleFavorite(Long userId, Long vehicleId) {
        // 1. 查询是否已经收藏
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId).eq(Favorite::getVehicleId, vehicleId);
        Favorite existFavorite = this.getOne(wrapper);

        if (existFavorite != null) {
            // 2. 如果已经收藏过了，再次点击就是“取消收藏”
            return this.removeById(existFavorite.getId());
        } else {
            // 3. 如果没收藏过，则执行“添加收藏”
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setVehicleId(vehicleId);
            return this.save(favorite);
        }
    }
}