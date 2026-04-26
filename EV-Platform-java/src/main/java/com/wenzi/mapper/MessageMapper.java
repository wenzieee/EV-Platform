package com.wenzi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wenzi.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}