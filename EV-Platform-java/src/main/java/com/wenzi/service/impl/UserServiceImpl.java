package com.wenzi.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wenzi.dto.UserLoginDTO;
import com.wenzi.dto.UserQueryDTO;
import com.wenzi.dto.UserRegisterDTO;
import com.wenzi.entity.User;
import com.wenzi.mapper.UserMapper;
import com.wenzi.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenzi.utils.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.wenzi.dto.UserStatsDTO;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 系统用户表 服务实现类
 * </p>
 *
 * @author 闻志博
 * @since 2026-03-21
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public void register(UserRegisterDTO dto) {
        // 1. 检查用户名是否已存在
        long count = this.count(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new RuntimeException("该用户名已被注册！");
        }

        // 2. 将数据落表
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setNickname(dto.getNickname());
        user.setRole((byte)1); // 默认 1 为普通用户
        user.setStatus((byte)1); // 默认 1 为正常状态

        // 3. 密码 BCrypt 加密处理 (核心安全步骤)
        String hashPassword = BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt());
        user.setPassword(hashPassword);

        this.save(user);
    }

    @Override
    public String login(UserLoginDTO dto) {
        // 1. 根据用户名查询用户
        User user = this.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null) {
            throw new RuntimeException("用户名不存在！");
        }

        // 🚀 核心修复：检查账号状态是否被封禁 (status: 0-禁用, 1-正常)
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("您的账号已被封禁，请联系管理员！");
        }

        // 2. 校验密码 (明文与数据库中的密文进行匹配)
        boolean isMatch = BCrypt.checkpw(dto.getPassword(), user.getPassword());
        if (!isMatch) {
            throw new RuntimeException("账号或密码错误！");
        }

        // 3. 登录成功，生成并返回 JWT Token
        return JwtUtils.createToken(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public Page<User> pageQuery(UserQueryDTO dto) {
        // 1. 构建分页对象
        Page<User> page = new Page<>(dto.getCurrent(), dto.getSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 3. 动态搜索：按用户名模糊查询
        if (StringUtils.isNotBlank(dto.getKeyword())) {
            wrapper.like(User::getUsername, dto.getKeyword());
        }

        // 4. 排序：把超级管理员排在最上面（role=0），然后按注册时间倒序
        wrapper.orderByAsc(User::getRole)
                .orderByDesc(User::getCreateTime);

        // 5. 执行查询
        return this.page(page, wrapper);
    }

    @Override
    public UserStatsDTO getUserStatistics() {
        // 1. 获取所有用户
        List<User> users = this.list();

        // 2. 统计按角色分类的数据
        List<UserStatsDTO.RoleStats> roleStatsList = users.stream()
                .filter(user -> user.getRole() != null)
                .collect(Collectors.groupingBy(User::getRole, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> {
                    UserStatsDTO.RoleStats roleStats = new UserStatsDTO.RoleStats();
                    roleStats.setRole(entry.getKey());
                    roleStats.setRoleText(convertUserRoleToText(entry.getKey()));
                    roleStats.setCount(entry.getValue());
                    return roleStats;
                })
                .collect(Collectors.toList());

        // 3. 统计按状态分类的数据
        List<UserStatsDTO.StatusStats> statusStatsList = users.stream()
                .filter(user -> user.getStatus() != null)
                .collect(Collectors.groupingBy(User::getStatus, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> {
                    UserStatsDTO.StatusStats statusStats = new UserStatsDTO.StatusStats();
                    statusStats.setStatus(entry.getKey());
                    statusStats.setStatusText(convertUserStatusToText(entry.getKey()));
                    statusStats.setCount(entry.getValue());
                    return statusStats;
                })
                .collect(Collectors.toList());

        // 4. 构建并返回 UserStatsDTO
        UserStatsDTO statsDTO = new UserStatsDTO();
        statsDTO.setRoleStats(roleStatsList);
        statsDTO.setStatusStats(statusStatsList);
        return statsDTO;
    }

    private String convertUserRoleToText(Byte role) {
        if (role == null) {
            return "未知角色";
        }
        switch (role) {
            case 0: return "超级管理员";
            case 1: return "普通用户";
            case 2: return "普通管理员";
            default: return "未知角色";
        }
    }

    private String convertUserStatusToText(Byte status) {
        if (status == null) {
            return "未知状态";
        }
        switch (status) {
            case 0: return "禁用";
            case 1: return "正常";
            default: return "未知状态";
        }
    }

    @Override
    @Transactional
    public void updateFollowingCount(Long userId, boolean increment) {
        User user = this.getById(userId);
        if (user != null) {
            Integer count = user.getFollowingCount();
            if (count == null) {
                count = 0;
            }
            if (increment) {
                user.setFollowingCount(count + 1);
            } else {
                user.setFollowingCount(Math.max(0, count - 1));
            }
            this.updateById(user);
        }
    }

    @Override
    @Transactional
    public void updateFollowerCount(Long userId, boolean increment) {
        User user = this.getById(userId);
        if (user != null) {
            Integer count = user.getFollowerCount();
            if (count == null) {
                count = 0;
            }
            if (increment) {
                user.setFollowerCount(count + 1);
            } else {
                user.setFollowerCount(Math.max(0, count - 1));
            }
            this.updateById(user);
        }
    }

}
