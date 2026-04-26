package com.wenzi.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWTUtil;
import com.wenzi.common.Result;
import com.wenzi.entity.Message;
import com.wenzi.service.IMessageService;
import com.wenzi.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private IMessageService messageService;

    @GetMapping("/my")
    public Result<List<Message>> getMyMessages(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (StrUtil.isBlank(token) || !JwtUtils.verifyToken(token)) {
            return Result.error("请先登录后再查看消息！");
        }

        try {
            Long userId = Long.valueOf(JWTUtil.parseToken(token).getPayload("id").toString());
            List<Message> messages = messageService.getMyMessages(userId);
            return Result.success(messages);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取消息失败：" + e.getMessage());
        }
    }

    @GetMapping("/unread/count")
    public Result<Integer> getUnreadCount(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (StrUtil.isBlank(token) || !JwtUtils.verifyToken(token)) {
            return Result.error("请先登录后再获取未读消息数！");
        }

        try {
            Long userId = Long.valueOf(JWTUtil.parseToken(token).getPayload("id").toString());
            Integer count = messageService.getUnreadCount(userId);
            return Result.success(count);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取未读消息数失败：" + e.getMessage());
        }
    }

    @PutMapping("/read/{id}")
    public Result<String> markAsRead(@PathVariable Long id) {
        try {
            messageService.markAsRead(id);
            return Result.success("标记已读成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("标记已读失败：" + e.getMessage());
        }
    }
}