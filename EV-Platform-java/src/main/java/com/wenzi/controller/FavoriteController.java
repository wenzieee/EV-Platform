package com.wenzi.controller;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wenzi.common.Result;
import com.wenzi.entity.Favorite;
import com.wenzi.entity.Vehicle;
import com.wenzi.service.IFavoriteService;
import com.wenzi.service.IVehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

//import javax.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    private IFavoriteService favoriteService;

    @Autowired
    private IVehicleService vehicleService;

    // 解析 Token 获取当前登录的用户 ID 的私有方法
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("未登录或Token已失效");
        }
        JWT jwt = JWTUtil.parseToken(token);
        return Long.valueOf(jwt.getPayload("id").toString());
    }

    /**
     * 接口1：切换收藏状态 (收藏/取消收藏)
     */
    @PostMapping("/toggle/{vehicleId}")
    public Result<String> toggleFavorite(@PathVariable Long vehicleId, HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            boolean isSuccess = favoriteService.toggleFavorite(userId, vehicleId);
            return isSuccess ? Result.success("操作成功") : Result.error("操作失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 接口2：查询当前用户是否收藏了某辆车 (用于前端判断星星是实心还是空心)
     */
    @GetMapping("/check/{vehicleId}")
    public Result<Boolean> checkFavorite(@PathVariable Long vehicleId, HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Favorite::getUserId, userId).eq(Favorite::getVehicleId, vehicleId);
            boolean isFavorited = favoriteService.count(wrapper) > 0;
            return Result.success(isFavorited);
        } catch (Exception e) {
            return Result.success(false); // 如果未登录，默认返回未收藏
        }
    }

    /**
     * 接口3：获取当前用户的全部收藏列表
     */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> myFavorites(HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Favorite::getUserId, userId).orderByDesc(Favorite::getCreateTime);
            List<Favorite> favorites = favoriteService.list(wrapper);

            // 构建包含车辆信息的返回数据
            List<Map<String, Object>> result = new ArrayList<>();
            for (Favorite favorite : favorites) {
                Vehicle vehicle = vehicleService.getById(favorite.getVehicleId());
                if (vehicle != null) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", favorite.getId());
                    item.put("userId", favorite.getUserId());
                    item.put("vehicleId", favorite.getVehicleId());
                    item.put("createTime", favorite.getCreateTime());
                    item.put("vehicleBrand", vehicle.getBrand());
                    item.put("vehicleModel", vehicle.getModel());
                    item.put("vehiclePrice", vehicle.getPrice());
                    item.put("vehicleDescription", "续航里程：" + vehicle.getRangeKm() + "km，驱动类型：" + vehicle.getDriveType());
                    item.put("vehicleImage", vehicle.getImageUrl());
                    result.add(item);
                }
            }
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取收藏列表失败：" + e.getMessage());
        }
    }
}