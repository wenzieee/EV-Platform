package com.wenzi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wenzi.dto.UserLoginDTO;
import com.wenzi.dto.UserQueryDTO;
import com.wenzi.dto.UserRegisterDTO;
import com.wenzi.dto.UserStatsDTO;
import com.wenzi.entity.User;

/**
 * <p>
 * 系统用户表 服务类
 * </p>
 *
 * @author 闻志博
 * @since 2026-03-21
 */
public interface IUserService extends IService<User> {

    // 注册业务
    void register(UserRegisterDTO dto);

    // 登录业务，返回 Token
    String login(UserLoginDTO dto);

    // 🚀 新增：分页查询用户
    Page<User> pageQuery(UserQueryDTO dto);

    // 🚀 新增：获取用户统计数据
    UserStatsDTO getUserStatistics();
}
