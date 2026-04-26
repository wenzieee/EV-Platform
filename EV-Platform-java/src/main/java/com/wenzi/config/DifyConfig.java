package com.wenzi.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class DifyConfig {

    @Value("${dify.api-url:https://api.dify.ai/v1/chat-messages}")
    private String apiUrl;

    @Value("${dify.app-key}")
    private String appKey;

    @Value("${dify.system-prompt:}") // 默认值为空字符串
    private String systemPrompt;
}
