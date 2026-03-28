package com.wenzi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wenzi.dto.UserLoginDTO;
import com.wenzi.dto.UserQueryDTO;
import com.wenzi.dto.UserRegisterDTO;
import com.wenzi.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
