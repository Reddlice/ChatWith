package org.reddlice.chatwith.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reddlice.chatwith.config.DefaultSettingConfig;
import org.reddlice.chatwith.dto.ChatRequest;
import org.reddlice.chatwith.dto.ChatResponse;
import org.reddlice.chatwith.service.ChatService;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName: ChatController
 * @description: TODO
 * @version: 1.0
 * @author: Redd_ice
 * @Date: 26/4/2026 下午 9:12
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final DefaultSettingConfig defaultSettingConfig;
    @PostMapping("/chat-with/send")
    public ChatResponse chat(@RequestBody ChatRequest chatRequest){
        log.info("request : {}" , chatRequest.getPrompt());
        chatRequest.setSkillName(defaultSettingConfig.getSkillName());
        chatRequest.setSystemPrompt(defaultSettingConfig.getSystemPrompt());
        return chatService.chat(chatRequest);
    }
}
