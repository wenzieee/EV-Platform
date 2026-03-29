package com.wenzi.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.jwt.JWTUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wenzi.common.Result;
import com.wenzi.dto.UserLoginDTO;
import com.wenzi.dto.UserQueryDTO;
import com.wenzi.dto.UserRegisterDTO;
import com.wenzi.entity.User;
import com.wenzi.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.wenzi.dto.UserStatsDTO;

//import javax.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequest;

/**
 * <p>
 * 系统用户表 前端控制器
 * </p>
 *
 * @author 闻志博
 * @since 2026-03-21
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;

    /**
     * 用户注册接口
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody UserRegisterDTO dto) {
        try {
            userService.register(dto);
            return Result.success("注册成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody UserLoginDTO dto) {
        try {
            String token = userService.login(dto);
            return Result.success("登录成功", token);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 分页查询用户列表
     */
    @PostMapping("/page")
    public Result<Page<User>> pageQuery(@RequestBody UserQueryDTO dto) {
        Page<User> pageResult = userService.pageQuery(dto);
        return Result.success(pageResult);
    }

    /**
     * 获取用户统计数据
     * 访问路径: GET http://localhost:8080/user/stats
     */
    @GetMapping("/stats")
    public Result<UserStatsDTO> getUserStats() {
        UserStatsDTO stats = userService.getUserStatistics();
        return Result.success(stats);
    }

    /**
     * 1. 修改用户状态或角色 (核心权限校验)
     */
    @PostMapping("/update")
    public Result<String> update(@RequestBody User targetUser, HttpServletRequest request) {
        try {
            // 解析当前操作者的身份
            String token = request.getHeader("token");
            if (StrUtil.isBlank(token)) return Result.error("未登录");
            Integer currentRole = Integer.valueOf(JWTUtil.parseToken(token).getPayload("role").toString());

            User dbUser = userService.getById(targetUser.getId());
            if (dbUser == null) return Result.error("用户不存在");

            // 🚨 防线1：超级管理员(0)不可被任何人修改！
            if (dbUser.getRole() == 0) {
                return Result.error("越权警告：超级管理员神圣不可侵犯！");
            }

            // 🚨 防线2：普通管理员(2)的限制
            if (currentRole == 2) {
                // 不能修改角色字段
                if (targetUser.getRole() != null && !targetUser.getRole().equals(dbUser.getRole())) {
                    return Result.error("越权操作：普通管理员无权分配角色！");
                }
                // 不能操作其他管理员
                if (dbUser.getRole() == 2 || dbUser.getRole() == 0) {
                    return Result.error("越权操作：普通管理员只能管理普通用户！");
                }
            }

            // 处理密码修改(如果有传)
            if (StrUtil.isNotBlank(targetUser.getPassword())) {
                targetUser.setPassword(BCrypt.hashpw(targetUser.getPassword(), BCrypt.gensalt()));
            }

            userService.updateById(targetUser);
            return Result.success("用户信息更新成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器异常：" + e.getMessage());
        }
    }

    /**
     * 2. 新增账号 (仅超级管理员可用)
     */
    @PostMapping("/add")
    public Result<String> add(@RequestBody User user, HttpServletRequest request) {
        try {
            String token = request.getHeader("token");
            Integer currentRole = Integer.valueOf(JWTUtil.parseToken(token).getPayload("role").toString());

            // 🚨 权限校验
            if (currentRole != 0) {
                return Result.error("越权操作：仅超级管理员(root)可新增账号！");
            }

            // 查重
            long count = userService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));
            if (count > 0) return Result.error("用户名已存在！");

            // 密码加密
            String pwd = StrUtil.isNotBlank(user.getPassword()) ? user.getPassword() : "123456";
            user.setPassword(BCrypt.hashpw(pwd, BCrypt.gensalt()));

            if (user.getStatus() == null) user.setStatus((byte) 1);

            userService.save(user);
            return Result.success("新增成功！默认密码为：" + pwd);
        } catch (Exception e) {
            return Result.error("服务器异常：" + e.getMessage());
        }
    }

    /**
     * 3. 删除账号 (仅超级管理员可用)
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("token");
            Integer currentRole = Integer.valueOf(JWTUtil.parseToken(token).getPayload("role").toString());

            if (currentRole != 0) {
                return Result.error("越权操作：仅超级管理员可删除账号！");
            }

            User dbUser = userService.getById(id);
            if (dbUser != null && dbUser.getRole() == 0) {
                return Result.error("禁止删除超级管理员！");
            }

            userService.removeById(id);
            return Result.success("删除成功！");
        } catch (Exception e) {
            return Result.error("服务器异常：" + e.getMessage());
        }
    }


}
