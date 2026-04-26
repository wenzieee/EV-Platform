package com.wenzi.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIResponse {
    private String id;
    private String model;
    private long created;
    private List<AIChoice> choices;
    private AIUsage usage;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIChoice {
        private int index;
        private AIMessage message;
        private String finish_reason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIUsage {
        private int prompt_tokens;
        private int completion_tokens;
        private int total_tokens;
    }
}
