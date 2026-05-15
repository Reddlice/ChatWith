package org.reddlice.chatwith.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reddlice.chatwith.component.AudioStore;
import org.reddlice.chatwith.component.SessionManager;
import org.reddlice.chatwith.config.DefaultSettingConfig;
import org.reddlice.chatwith.dto.ChatLogRequest;
import org.reddlice.chatwith.dto.ChatRequest;
import org.reddlice.chatwith.dto.ChatResponse;
import org.reddlice.chatwith.dto.SessionInfo;
import org.reddlice.chatwith.service.ChatLogService;
import org.reddlice.chatwith.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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
    private final ChatLogService chatLogService;
    private final DefaultSettingConfig defaultSettingConfig;
    private final AudioStore audioStore;
    private final SessionManager sessionManager;

    @PostMapping("/chat-with/send")
    public ChatResponse chat(@RequestBody ChatRequest chatRequest){
        log.info("request : {}" , chatRequest.getPrompt());
        chatRequest.setSkillName(defaultSettingConfig.getSkillName());
        chatRequest.setSystemPrompt(defaultSettingConfig.getSystemPrompt());
        return chatService.chat(chatRequest);
    }

    @GetMapping("/chat-with/audio/{messageId}")
    public ResponseEntity<Map<String, Object>> getAudio(@PathVariable String messageId) {
        String audio = audioStore.get(messageId);
        if (audio == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(Map.of("audioBase64", audio, "ready", true));
    }

    @GetMapping("/chat-with/sessions")
    public List<SessionInfo> listSessions() {
        return sessionManager.listSessions();
    }

    @PostMapping("/chat-with/sessions")
    public SessionInfo createSession(@RequestBody Map<String, String> body) {
        return sessionManager.createSession(body.get("name"));
    }

    @PutMapping("/chat-with/sessions/{id}")
    public void renameSession(@PathVariable String id, @RequestBody Map<String, String> body) {
        sessionManager.renameSession(id, body.get("name"));
    }

    @DeleteMapping("/chat-with/sessions/{id}")
    public void deleteSession(@PathVariable String id) {
        sessionManager.deleteSession(id);
    }

    @PutMapping("/chat-with/sessions/{id}/activate")
    public void activateSession(@PathVariable String id) {
        sessionManager.activateSession(id);
    }

    @PostMapping("/chat-with/log")
    public void saveLog(@RequestBody ChatLogRequest request) {
        chatLogService.saveLog(request);
    }
}
