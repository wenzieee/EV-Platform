package com.wenzi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("biz_dealer")
public class Dealer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String brand;
    private String province;
    private String city;
    private String address;
    private String phone;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer status;
    private Date createTime;

    // 🚀 核心：这是一个非数据库字段，用来专门接收 MySQL 计算出来的“距离(米)”
    @TableField(exist = false)
    private Double distance;
}
