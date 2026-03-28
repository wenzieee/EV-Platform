package com.wenzi.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wenzi.common.Result;
import com.wenzi.dto.VehicleQueryDTO;
import com.wenzi.entity.Vehicle;
import com.wenzi.service.IVehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    // 注入车辆业务逻辑层接口
    @Autowired
    private IVehicleService vehicleService;

    /**
     * 查询所有车辆列表数据
     * 访问路径: GET http://localhost:8080/vehicle/list
     */
    @GetMapping("/list")
    public Result<List<Vehicle>> list() {
        // 调用 MyBatis-Plus 封装好的 list() 方法，直接查询 biz_vehicle 表所有数据
        List<Vehicle> vehicleList = vehicleService.list();

        // 使用我们刚刚封装的统一返回类，将结果包裹起来返回给前端
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
     * 根据 ID 查询车辆详情
     * 访问路径: GET http://localhost:8080/vehicle/{id}
     */
    @GetMapping("/{id}")
    public Result<Vehicle> getById(@PathVariable Integer id) {
        Vehicle vehicle = vehicleService.getById(id);
        return Result.success(vehicle);
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
     * 新增车辆接口
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody Vehicle vehicle) {
        try {
            // 🚀 核心修复 1：使用 LocalDateTime.now() 注入当前创建时间
            if (vehicle.getCreateTime() == null) {
                vehicle.setCreateTime(LocalDateTime.now());
            }

            // 顺手把更新时间也初始化一下，保持数据规范
            if (vehicle.getUpdateTime() == null) {
                vehicle.setUpdateTime(LocalDateTime.now());
            }

            // 🚀 核心修复 2：如果前端没传热度，给个初始值 0
            if (vehicle.getHotScore() == null) {
                vehicle.setHotScore(0);
            }

            // 执行保存
            boolean success = vehicleService.save(vehicle);
            if (success) {
                return Result.success("新增车辆成功！");
            } else {
                return Result.error("新增失败，请稍后再试");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器异常：" + e.getMessage());
        }
    }

    /**
     * 修改车辆接口
     */
    @PostMapping("/update")
    public Result<String> update(@RequestBody Vehicle vehicle) {
        try {
            // 核心：修改时更新updateTime为当前时间
            vehicle.setUpdateTime(LocalDateTime.now());
            // MyBatis-Plus 提供的 updateById 方法，会自动根据传入的 id 更新其他非空字段
            boolean success = vehicleService.updateById(vehicle);
            if (success) {
                return Result.success("修改车辆成功！");
            } else {
                return Result.error("修改失败，找不到对应的车辆数据");
            }
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
            // MyBatis-Plus 提供的 removeById 方法
            // 如果你的 Vehicle 实体类里 is_deleted 字段加了 @TableLogic 注解，这里会自动变成逻辑删除 (UPDATE is_deleted = 1)
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