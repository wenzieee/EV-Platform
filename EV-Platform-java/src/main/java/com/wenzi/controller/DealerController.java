package com.wenzi.controller;

import com.wenzi.common.Result;
import com.wenzi.entity.Dealer;
import com.wenzi.service.IDealerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dealer")
public class DealerController {

    @Autowired
    private IDealerService dealerService;

    /**
     * 获取距离用户最近的品牌4S店
     * 示例请求: /dealer/nearest?brand=阿维塔&lng=113.62&lat=34.75
     */
    @GetMapping("/nearest")
    public Result<List<Dealer>> getNearestDealers(
            @RequestParam String brand, // 重新添加 brand 参数
            @RequestParam Double lng,
            @RequestParam Double lat) {

        try {
            // 参数校验
            if (brand == null || brand.isEmpty() || lng == null || lat == null) { // 增加 brand 参数校验
                return Result.error("品牌信息或地理位置缺失");
            }

            List<Dealer> list = dealerService.getNearestDealers(lng, lat, brand);

            // 选做：可以把返回的 distance(米) 转化为公里(km) 方便前端直接展示
            for (Dealer dealer : list) {
                if (dealer.getDistance() != null) {
                    double km = dealer.getDistance() / 1000.0;
                    // 保留一位小数，如 2.5 km
                    dealer.setDistance(Math.round(km * 10.0) / 10.0);
                }
            }

            return Result.success(list);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取附近门店失败");
        }
    }
}