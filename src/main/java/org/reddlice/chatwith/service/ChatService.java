package org.reddlice.chatwith.service;

import org.reddlice.chatwith.dto.ChatRequest;
import org.reddlice.chatwith.dto.ChatResponse;
import org.springframework.stereotype.Service;

/**
 * @ClassName: ChatService
 * @description: TODO
 * @version: 1.0
 * @author: Redd_ice
 * @Date: 26/4/2026 下午 9:16
 */
@Service
public interface ChatService {
    /**
     * 聊天接口
     * @return
     */
    ChatResponse chat(ChatRequest chatRequest);
}
