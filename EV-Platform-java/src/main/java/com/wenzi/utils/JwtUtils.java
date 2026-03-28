package com.wenzi.utils;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.signers.JWTSignerUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 */
public class JwtUtils {

    // 绝密秘钥（实际企业开发中应配置在 application.yml 里）
    private static final byte[] KEY = "Wenzi_EvPlatform_Secret_Key_2026".getBytes();

    /**
     * 生成 Token
     * @param userId 用户ID
     * @param username 用户名
     * @return token字符串
     */
    public static String createToken(Long userId, String username,Object role) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", userId);
        payload.put("username", username);
        payload.put("role", role);
        // 设置过期时间为 24 小时后
        payload.put("expire_time", System.currentTimeMillis() + 1000 * 60 * 60 * 24);

        return JWTUtil.createToken(payload, KEY);
    }

    /**
     * 验证 Token 是否合法
     */
    public static boolean verifyToken(String token) {
        try {
            return JWTUtil.verify(token, KEY);
        } catch (Exception e) {
            return false;
        }
    }
}