package org.reddlice.chatwith.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.reddlice.chatwith.dto.ChatLogRequest;
import org.reddlice.chatwith.service.ChatLogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Slf4j
public class ChatLogServiceImpl implements ChatLogService {

    @Value("${chat.log-dir:chat-logs}")
    private String logDir;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @PostConstruct
    public void init() {
        File dir = new File(logDir);
        if (!dir.exists()) {
            dir.mkdirs();
            log.info("聊天日志目录已创建: {}", dir.getAbsolutePath());
        }
    }

    @Override
    public void saveLog(ChatLogRequest request) {
        if (request.getSessionId() == null || request.getMessages() == null || request.getMessages().isEmpty()) {
            return;
        }

        File file = new File(logDir, request.getSessionId() + ".jsonl");
        boolean isNew = !file.exists();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            // 新文件先写会话头
            if (isNew) {
                Map<String, Object> header = new LinkedHashMap<>();
                header.put("type", "session");
                header.put("id", request.getSessionId());
                header.put("name", request.getSessionName() != null ? request.getSessionName() : "");
                header.put("created_at", request.getCreatedAt() != null ? request.getCreatedAt() : "");
                header.put("updated_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                writer.write(objectMapper.writeValueAsString(header));
                writer.newLine();
            }

            // 追加本次消息
            for (Map<String, String> msg : request.getMessages()) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("type", "message");
                entry.put("role", msg.get("role"));
                entry.put("content", msg.get("content"));
                entry.put("timestamp", msg.get("timestamp"));
                writer.write(objectMapper.writeValueAsString(entry));
                writer.newLine();
            }

            log.info("聊天日志已追加: {} (新文件={})", file.getAbsolutePath(), isNew);
        } catch (IOException e) {
            log.error("保存聊天日志失败: {}", request.getSessionId(), e);
        }
    }
}
