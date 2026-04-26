package com.wenzi.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DifyChatResponse {
    private String event;
    private String task_id;
    private String answer;
    private String conversation_id;
    private String id;
    private Long created_at;
    private String message_id;
    private String error;
}
