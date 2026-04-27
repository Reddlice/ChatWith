package org.reddlice.chatwith.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 请求类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRequest {

    private String prompt;
    private String skillName;
    private String systemPrompt;

}
