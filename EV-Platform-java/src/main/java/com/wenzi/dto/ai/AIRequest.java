package com.wenzi.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIRequest {
    private String model = "qwen-turbo"; // 默认使用通义千问-turbo模型
    private List<AIMessage> messages;
}
