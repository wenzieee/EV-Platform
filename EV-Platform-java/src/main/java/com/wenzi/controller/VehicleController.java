package com.wenzi.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wenzi.common.Result;
import com.wenzi.dto.VehicleDetailDTO;
import com.wenzi.dto.VehicleQueryDTO;
import com.wenzi.dto.VehicleStatsDTO;
import com.wenzi.entity.Vehicle;
import com.wenzi.entity.VehicleTrim;
import com.wenzi.service.IVehicleService;
import com.wenzi.service.IVehicleTrimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 新能源汽车信息表 前端控制器
 * </p>
 *
 * @author 闻志博
 */
@RestController
@RequestMapping("/vehicle")
public class VehicleController {

    @Autowired
    private IVehicleService vehicleService;

    @Autowired
    private IVehicleTrimService vehicleTrimService;

    /**
     * 查询所有车辆列表数据
     * 访问路径: GET http://localhost:8080/vehicle/list
     */
    @GetMapping("/list")
    public Result<List<Vehicle>> list() {
        List<Vehicle> vehicleList = vehicleService.list();
        return Result.success(vehicleList);
    }

    /**
     * 多条件分页查询车辆列表
     * 访问路径: POST http://localhost:8080/vehicle/page
     */
    @PostMapping("/page")
    public Result<Page<Vehicle>> pageQuery(@RequestBody VehicleQueryDTO queryDTO) {
        Page<Vehicle> pageResult = vehicleService.pageQuery(queryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据 ID 查询车辆详情（包含配置列表）
     * 访问路径: GET http://localhost:8080/vehicle/{id}
     */
    @GetMapping("/{id}")
    public Result<VehicleDetailDTO> getById(@PathVariable Long id) {
        Vehicle vehicle = vehicleService.getById(id);
        if (vehicle == null) {
            return Result.error("车辆不存在");
        }
        List<VehicleTrim> trims = vehicleTrimService.getTrimsByVehicleId(id);
        VehicleDetailDTO detail = new VehicleDetailDTO(vehicle, trims);
        return Result.success(detail);
    }

    /**
     * 获取热门推荐前 6 名
     * 访问路径: GET http://localhost:8080/vehicle/hot
     */
    @GetMapping("/hot")
    public Result<List<Vehicle>> getHotList() {
        List<Vehicle> hotList = vehicleService.getHotVehicles();
        return Result.success(hotList);
    }

    /**
     * 获取车辆统计数据
     * 访问路径: GET http://localhost:8080/vehicle/stats
     */
    @GetMapping("/stats")
    public Result<VehicleStatsDTO> getVehicleStats() {
        VehicleStatsDTO stats = vehicleService.getVehicleStatistics();
        return Result.success(stats);
    }

    /**
     * 根据车辆ID获取车型配置列表
     * 访问路径: GET http://localhost:8080/vehicle/trims/{vehicleId}
     */
    @GetMapping("/trims/{vehicleId}")
    public Result<List<VehicleTrim>> getTrimsByVehicleId(@PathVariable Long vehicleId) {
        List<VehicleTrim> trims = vehicleTrimService.getTrimsByVehicleId(vehicleId);
        return Result.success(trims);
    }

    /**
     * 新增车辆接口（包含配置）
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody VehicleDetailDTO detailDTO) {
        try {
            Vehicle vehicle = detailDTO.getVehicle();
            
            if (vehicle.getCreateTime() == null) {
                vehicle.setCreateTime(LocalDateTime.now());
            }
            if (vehicle.getUpdateTime() == null) {
                vehicle.setUpdateTime(LocalDateTime.now());
            }
            if (vehicle.getHotScore() == null) {
                vehicle.setHotScore(0);
            }
            if (vehicle.getStatus() == null) {
                vehicle.setStatus((byte) 1);
            }

            boolean success = vehicleService.save(vehicle);
            if (!success) {
                return Result.error("新增失败，请稍后再试");
            }

            // 保存配置
            List<VehicleTrim> trims = detailDTO.getTrims();
            if (trims != null && !trims.isEmpty()) {
                vehicleTrimService.saveTrims(vehicle.getId(), trims);
                
                // 更新车辆的价格区间
                BigDecimal minPrice = null;
                BigDecimal maxPrice = null;
                for (VehicleTrim trim : trims) {
                    if (trim.getPrice() != null) {
                        if (minPrice == null || trim.getPrice().compareTo(minPrice) < 0) {
                            minPrice = trim.getPrice();
                        }
                        if (maxPrice == null || trim.getPrice().compareTo(maxPrice) > 0) {
                            maxPrice = trim.getPrice();
                        }
                    }
                }
                vehicle.setMinPrice(minPrice);
                vehicle.setMaxPrice(maxPrice);
                vehicleService.updateById(vehicle);
            }

            return Result.success("新增车辆成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器异常：" + e.getMessage());
        }
    }

    /**
     * 修改车辆接口（包含配置）
     */
    @PostMapping("/update")
    public Result<String> update(@RequestBody VehicleDetailDTO detailDTO) {
        try {
            Vehicle vehicle = detailDTO.getVehicle();
            vehicle.setUpdateTime(LocalDateTime.now());

            boolean success = vehicleService.updateById(vehicle);
            if (!success) {
                return Result.error("修改失败，找不到对应的车辆数据");
            }

            // 保存配置
            List<VehicleTrim> trims = detailDTO.getTrims();
            if (trims != null && !trims.isEmpty()) {
                vehicleTrimService.saveTrims(vehicle.getId(), trims);
                
                // 更新车辆的价格区间
                BigDecimal minPrice = null;
                BigDecimal maxPrice = null;
                for (VehicleTrim trim : trims) {
                    if (trim.getPrice() != null) {
                        if (minPrice == null || trim.getPrice().compareTo(minPrice) < 0) {
                            minPrice = trim.getPrice();
                        }
                        if (maxPrice == null || trim.getPrice().compareTo(maxPrice) > 0) {
                            maxPrice = trim.getPrice();
                        }
                    }
                }
                vehicle.setMinPrice(minPrice);
                vehicle.setMaxPrice(maxPrice);
                vehicleService.updateById(vehicle);
            }

            return Result.success("修改车辆成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器异常：" + e.getMessage());
        }
    }

    /**
     * 删除车辆接口 (RESTful 风格)
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        try {
            // 先删除配置
            vehicleTrimService.removeByVehicleId(id);
            
            // 再删除车辆
            boolean success = vehicleService.removeById(id);
            if (success) {
                return Result.success("删除成功！");
            } else {
                return Result.error("删除失败，该车辆可能已被删除");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器异常：" + e.getMessage());
        }
    }
}