package com.wenzi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wenzi.entity.Message;
import com.wenzi.mapper.MessageMapper;
import com.wenzi.service.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl implements IMessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public void sendMessage(Long senderId, Long receiverId, String content) {
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);
    }

    @Override
    public List<Message> getMyMessages(Long userId) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver_id", userId)
                .orderByDesc("create_time");
        return messageMapper.selectList(wrapper);
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver_id", userId)
                .eq("is_read", 0);
        return messageMapper.selectCount(wrapper).intValue();
    }

    @Override
    public void markAsRead(Long messageId) {
        Message message = new Message();
        message.setId(messageId);
        message.setIsRead(1);
        messageMapper.updateById(message);
    }
}