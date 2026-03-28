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
 * 系统用户表
 * </p>
 *
 * @author 闻志博
 * @since 2026-03-21
 */
@Getter
@Setter
@TableName("sys_user")
@ApiModel(value = "User对象", description = "系统用户表")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户名/账号")
    @TableField("username")
    private String username;

    @ApiModelProperty("密码(推荐使用 BCrypt 加密存储)")
    @TableField("password")
    private String password;

    @ApiModelProperty("昵称")
    @TableField("nickname")
    private String nickname;

    @ApiModelProperty("联系电话")
    @TableField("phone")
    private String phone;

    @ApiModelProperty("角色: 0-管理员, 1-普通用户")
    @TableField("role")
    private Byte role;

    @ApiModelProperty("状态: 0-禁用, 1-正常")
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
