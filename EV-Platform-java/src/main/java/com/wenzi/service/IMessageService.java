package com.wenzi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wenzi.entity.Message;

import java.util.List;

public interface IMessageService {
    void sendMessage(Long senderId, Long receiverId, String content);
    List<Message> getMyMessages(Long userId);
    Integer getUnreadCount(Long userId);
    void markAsRead(Long messageId);
}