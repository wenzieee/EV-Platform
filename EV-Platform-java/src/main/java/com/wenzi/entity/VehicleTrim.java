package com.wenzi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 车型配置表（SKU）
 * </p>
 *
 * @author 闻志博
 * @since 2026-05-05
 */
@Getter
@Setter
@TableName("biz_vehicle_trim")
@ApiModel(value = "VehicleTrim对象", description = "车型配置表（SKU）")
public class VehicleTrim implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("车辆ID（关联biz_vehicle表）")
    @TableField("vehicle_id")
    private Long vehicleId;

    @ApiModelProperty("配置名称(如：标准版、豪华版、旗舰版)")
    @TableField("trim_name")
    private String trimName;

    @ApiModelProperty("指导价格(万元)")
    @TableField("price")
    private BigDecimal price;

    @ApiModelProperty("续航里程(km)")
    @TableField("range_km")
    private Integer rangeKm;

    @ApiModelProperty("电池容量(kWh)")
    @TableField("battery_capacity")
    private BigDecimal batteryCapacity;

    @ApiModelProperty("电机功率(kW)")
    @TableField("motor_power")
    private Integer motorPower;

    @ApiModelProperty("最高时速(km/h)")
    @TableField("max_speed")
    private Integer maxSpeed;

    @ApiModelProperty("加速时间(0-100km/h，秒)")
    @TableField("acceleration_time")
    private BigDecimal accelerationTime;

    @ApiModelProperty("充电时间(快充0-80%，分钟)")
    @TableField("charge_time")
    private Integer chargeTime;

    @ApiModelProperty("车身颜色(多个用逗号分隔)")
    @TableField("colors")
    private String colors;

    @ApiModelProperty("配置详情(JSON格式)")
    @TableField("config_details")
    private String configDetails;

    @ApiModelProperty("驱动方式：后驱/四驱")
    @TableField("drive_type")
    private String driveType;

    @ApiModelProperty("是否主推: 0-否, 1-是")
    @TableField("is_hot")
    private Byte isHot;

    @ApiModelProperty("状态: 0-下架, 1-上架")
    @TableField("status")
    private Byte status;

    @ApiModelProperty("创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;

    @ApiModelProperty("逻辑删除: 0-未删除, 1-已删除")
    @TableField("is_deleted")
    private Byte isDeleted;
}