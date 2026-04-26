package com.wenzi.controller;

import com.wenzi.common.Result;
import com.wenzi.common.Result;
import com.wenzi.config.DifyConfig;
import com.wenzi.dto.ai.AIMessage;
import com.wenzi.dto.ai.AIRequest;
import com.wenzi.service.AIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/ai")
@Slf4j
public class AIController {

    @Autowired
    private AIService aiService;

    @Autowired
    private DifyConfig difyConfig; // 注入DifyConfig

    @PostMapping("/chat")
    public Result<String> chat(@RequestBody AIMessage userMessage) {
        try {
            // AIService现在直接接收AIMessage并处理Dify的请求构建，包括系统提示词
            String aiResponseContent = aiService.getAIResponse(userMessage);
            return Result.success(aiResponseContent);
        } catch (Exception e) {
            log.error("AI 聊天接口调用失败: {}", e.getMessage(), e);
            return Result.error("AI 聊天服务异常，请稍后再试。" + e.getMessage()); // 错误信息中包含详细异常
        }
    }
}
