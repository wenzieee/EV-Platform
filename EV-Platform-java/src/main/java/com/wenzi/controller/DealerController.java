package com.wenzi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wenzi.common.Result;
import com.wenzi.entity.Dealer;
import com.wenzi.mapper.DealerMapper;
import com.wenzi.service.IDealerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dealer")
public class DealerController {

    @Autowired
    private IDealerService dealerService;

    @Autowired
    private DealerMapper dealerMapper;

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

    /**
     * 分页查询门店列表（管理端）
     */
    @GetMapping("/page")
    public Result<IPage<Dealer>> pageQuery(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        try {
            Page<Dealer> page = new Page<>(pageNum, pageSize);
            IPage<Dealer> result = dealerService.pageQuery(page, keyword);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取门店列表失败");
        }
    }

    /**
     * 根据ID查询门店详情
     */
    @GetMapping("/{id}")
    public Result<Dealer> getById(@PathVariable Long id) {
        try {
            Dealer dealer = dealerService.getById(id);
            if (dealer == null) {
                return Result.error("门店不存在");
            }
            return Result.success(dealer);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取门店详情失败");
        }
    }

    /**
     * 新增门店
     */
    @PostMapping
    public Result<String> save(@RequestBody Dealer dealer) {
        try {
            dealerService.save(dealer);
            return Result.success("新增成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("新增失败");
        }
    }

    /**
     * 更新门店信息
     */
    @PutMapping
    public Result<String> update(@RequestBody Dealer dealer) {
        try {
            dealerService.updateById(dealer);
            return Result.success("更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新失败");
        }
    }

    /**
     * 删除门店
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        try {
            dealerService.removeById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败");
        }
    }

    /**
     * 获取经销商统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 按省份统计
            List<Map<String, Object>> provinceStats = new ArrayList<>();
            try {
                List<com.wenzi.dto.DealerStatsDTO> provinceList = dealerMapper.statsByProvince();
                for (com.wenzi.dto.DealerStatsDTO item : provinceList) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("province", item.getProvince());
                    map.put("count", item.getCount());
                    provinceStats.add(map);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            stats.put("provinceStats", provinceStats);
            
            // 按状态统计
            List<Map<String, Object>> statusStats = new ArrayList<>();
            try {
                List<com.wenzi.dto.DealerStatsDTO> statusList = dealerMapper.statsByStatus();
                for (com.wenzi.dto.DealerStatsDTO item : statusList) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", item.getStatus());
                    map.put("count", item.getCount());
                    statusStats.add(map);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            stats.put("statusStats", statusStats);
            
            // 按品牌统计
            List<Map<String, Object>> brandStats = new ArrayList<>();
            try {
                List<com.wenzi.dto.DealerStatsDTO> brandList = dealerMapper.statsByBrand();
                for (com.wenzi.dto.DealerStatsDTO item : brandList) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("brand", item.getBrand());
                    map.put("count", item.getCount());
                    brandStats.add(map);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            stats.put("brandStats", brandStats);
            
            // 按城市统计
            List<Map<String, Object>> cityStats = new ArrayList<>();
            try {
                List<com.wenzi.dto.DealerStatsDTO> cityList = dealerMapper.statsByCity();
                for (com.wenzi.dto.DealerStatsDTO item : cityList) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("city", item.getCity());
                    map.put("count", item.getCount());
                    cityStats.add(map);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            stats.put("cityStats", cityStats);
            
            return Result.success(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取统计数据失败");
        }
    }
}