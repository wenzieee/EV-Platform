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
 * 新能源汽车信息表
 * </p>
 *
 * @author 闻志博
 * @since 2026-03-21
 */
@Getter
@Setter
@TableName("biz_vehicle")
@ApiModel(value = "Vehicle对象", description = "新能源汽车信息表")
public class Vehicle implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("品牌(如：比亚迪、特斯拉)")
    @TableField("brand")
    private String brand;

    @ApiModelProperty("车型名称(如：汉EV、Model 3)")
    @TableField("model")
    private String model;

    @ApiModelProperty("指导价格(万元)")
    @TableField("price")
    private BigDecimal price;

    @ApiModelProperty("续航里程(km)")
    @TableField("range_km")
    private Integer rangeKm;

    @ApiModelProperty("驱动类型(如：前置前驱、双电机四驱)")
    @TableField("drive_type")
    private String driveType;

    @ApiModelProperty("车辆主图URL")
    @TableField("image_url")
    private String imageUrl;

    @ApiModelProperty("详细配置描述(建议前端传JSON格式或富文本)")
    @TableField("configuration")
    private String configuration;

    @ApiModelProperty("上架状态: 0-下架, 1-上架")
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

    @ApiModelProperty("热度值")
    @TableField("hot_score")
    private Integer hotScore;
}
