package com.wenzi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 购车意向订单表
 * </p>
 *
 * @author 闻志博
 * @since 2026-03-21
 */
@Getter
@Setter
@TableName("biz_intent_order")
@ApiModel(value = "IntentOrder对象", description = "购车意向订单表")
public class IntentOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("提交意向的用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty("意向车型ID")
    @TableField("vehicle_id")
    private Long vehicleId;

    @ApiModelProperty("联系电话")
    @TableField("contact_phone")
    private String contactPhone;

    @ApiModelProperty("用户留言")
    @TableField("message")
    private String message;

    @ApiModelProperty("跟进状态: 0-待跟进, 1-跟进中, 2-已成交, 3-已取消")
    @TableField("status")
    private Byte status;

    @ApiModelProperty("提交时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;

    @ApiModelProperty("逻辑删除: 0-未删除, 1-已删除")
    @TableField("is_deleted")
    private Byte isDeleted;

    // 🚀 核心补充：这不是数据库表里的字段，纯粹是为了传给前端展示用的
    @TableField(exist = false)
    private String brand;

    @TableField(exist = false)
    private String model;
}
