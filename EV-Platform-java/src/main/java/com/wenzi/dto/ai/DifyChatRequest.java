package com.wenzi.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DifyChatRequest {
    private Map<String, Object> inputs; // 可以是空对象
    private String query;
    private String response_mode = "blocking"; // 默认为阻塞模式
    private String conversation_id; // 可选，用于多轮对话
    private String user = "default_user"; // 用户标识，可根据实际用户ID设置
    private boolean stream = false; // 默认为非流式传输
}
