package org.reddlice.chatwith.dto;

import lombok.Data;

/**
 * @ClassName: LettaAgentCreateRequest
 * @version: 1.0
 * @author: Redd_ice
 * @Date: 24/7/2026 上午 2:47 
 * 
 */
@Data
public class LettaAgentCreateRequest {
    private String name;    // Agent 名称
    private String system;  // 系统提示词
}
