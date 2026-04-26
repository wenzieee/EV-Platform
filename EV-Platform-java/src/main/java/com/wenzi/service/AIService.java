package com.wenzi.service;

import com.wenzi.config.DifyConfig;
import com.wenzi.dto.ai.AIRequest;
import com.wenzi.dto.ai.AIMessage;
import com.wenzi.dto.ai.AIResponse;
import com.wenzi.dto.ai.DifyChatRequest;
import com.wenzi.dto.ai.DifyChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AIService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DifyConfig difyConfig;

    public String getAIResponse(AIMessage userMessage) {
        // 构建Dify请求
        DifyChatRequest request = new DifyChatRequest();
        // Dify的system prompt通常在Dify应用中配置，如果需要动态传入，可以考虑拼接query或使用inputs
        // 这里为了兼容之前的逻辑，将system prompt拼接到query前
        String finalQuery = userMessage.getContent();
        String systemPrompt = difyConfig.getSystemPrompt();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            finalQuery = systemPrompt + "\n" + finalQuery;
        }
        request.setQuery(finalQuery);
        request.setUser("default_user"); // 可以根据实际用户ID进行设置
        request.setResponse_mode("blocking");
        request.setStream(false);
        request.setInputs(new HashMap<>()); // 保持inputs为空对象

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(difyConfig.getAppKey());

        HttpEntity<DifyChatRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<DifyChatResponse> responseEntity = restTemplate.postForEntity(
                    difyConfig.getApiUrl(),
                    entity,
                    DifyChatResponse.class
            );

            DifyChatResponse difyResponse = responseEntity.getBody();
            if (difyResponse != null && difyResponse.getAnswer() != null) {
                return difyResponse.getAnswer();
            } else {
                // 尝试从error字段获取信息
                if (difyResponse != null && difyResponse.getError() != null) {
                    return "Dify API 错误: " + difyResponse.getError();
                } else {
                    return "Dify API 返回异常或为空。";
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("调用 Dify API 失败: " + e.getMessage(), e);
        }
    }
}
