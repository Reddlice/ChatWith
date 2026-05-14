package org.reddlice.chatwith.component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.reddlice.chatwith.config.ClaudeCodeConfig;
import org.reddlice.chatwith.dto.SessionInfo;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class SessionManager {

    private final ClaudeCodeConfig claudeCodeConfig;
    private final ObjectMapper objectMapper;

    private List<SessionInfo> sessions = new ArrayList<>();
    private String activeSessionId;
    private final Set<String> resumedSessions = new HashSet<>();
    private Path sessionsFile;
    private Path sessionIdFile;

    public SessionManager(ClaudeCodeConfig claudeCodeConfig) {
        this.claudeCodeConfig = claudeCodeConfig;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @PostConstruct
    public void init() {
        Path skillsParent = Paths.get(claudeCodeConfig.getSkillsDir()).getParent();
        sessionsFile = skillsParent.resolve("sessions.json");
        sessionIdFile = skillsParent.resolve(".chatwith-session");

        try {
            Files.createDirectories(skillsParent);
        } catch (IOException e) {
            log.error("无法创建目录: {}", skillsParent, e);
        }

        // 加载已有会话列表
        if (Files.isRegularFile(sessionsFile)) {
            loadSessions();
        }

        // 读取当前活跃会话ID
        if (Files.isRegularFile(sessionIdFile)) {
            try {
                activeSessionId = Files.readString(sessionIdFile).trim();
            } catch (IOException e) {
                log.warn("读取活跃会话文件失败", e);
            }
        }

        // 迁移：旧项目只有 .chatwith-session，没有 sessions.json
        if (sessions.isEmpty() && activeSessionId != null) {
            SessionInfo defaultSession = new SessionInfo();
            defaultSession.setId(activeSessionId);
            defaultSession.setName("默认会话");
            defaultSession.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            defaultSession.setResumed(true); // 已有会话，认为已经启动过
            sessions.add(defaultSession);
            saveSessions();
            log.info("已从旧格式迁移会话: {}", activeSessionId);
        }

        // 如果没有任何会话，创建默认的
        if (sessions.isEmpty()) {
            SessionInfo defaultSession = new SessionInfo();
            defaultSession.setId(UUID.randomUUID().toString());
            defaultSession.setName("默认会话");
            defaultSession.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            defaultSession.setResumed(false);
            sessions.add(defaultSession);
            activeSessionId = defaultSession.getId();
            saveSessions();
            writeActiveSessionId();
            log.info("创建默认会话: {}", activeSessionId);
        }

        // 如果活跃会话不在会话列表中（被删了），切换到第一个
        if (activeSessionId != null && findSession(activeSessionId) == null) {
            activeSessionId = sessions.get(0).getId();
            writeActiveSessionId();
        }
        if (activeSessionId == null && !sessions.isEmpty()) {
            activeSessionId = sessions.get(0).getId();
            writeActiveSessionId();
        }

        // 恢复 resumed 标记
        for (SessionInfo s : sessions) {
            if (s.isResumed()) {
                resumedSessions.add(s.getId());
            }
        }

        log.info("会话管理器初始化完成，活跃会话: {}, 共 {} 个会话", activeSessionId, sessions.size());
    }

    /**
     * 返回会话列表中的数据
     * @return
     */
    public List<SessionInfo> listSessions() {
        return new ArrayList<>(sessions);
    }

    /**
     * 创建会话
     * @param name
     * @return
     */
    public synchronized SessionInfo createSession(String name) {
        SessionInfo session = new SessionInfo();
        session.setId(UUID.randomUUID().toString());
        session.setName(name != null && !name.isBlank() ? name : "新会话");
        session.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        session.setResumed(false);
        sessions.add(0, session);
        saveSessions();
        log.info("创建会话: {} ({})", session.getName(), session.getId());
        return session;
    }

    /**
     * 重命名会话
     * @param id
     * @param name
     */
    public synchronized void renameSession(String id, String name) {
        SessionInfo session = findSession(id);
        if (session != null && name != null && !name.isBlank()) {
            session.setName(name);
            saveSessions();
        }
    }

    /**
     * 删除会话
     * @param id
     */
    public synchronized void deleteSession(String id) {
        SessionInfo session = findSession(id);
        if (session == null) return;
        sessions.remove(session);
        resumedSessions.remove(id);

        // 如果删除的是当前活跃会话，切到第一个
        if (id.equals(activeSessionId)) {
            if (!sessions.isEmpty()) {
                activeSessionId = sessions.get(0).getId();
            } else {
                // 全删了，创建新默认
                SessionInfo newSession = new SessionInfo();
                newSession.setId(UUID.randomUUID().toString());
                newSession.setName("默认会话");
                newSession.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                newSession.setResumed(false);
                sessions.add(newSession);
                activeSessionId = newSession.getId();
            }
            writeActiveSessionId();
        }
        saveSessions();
    }

    public synchronized void activateSession(String id) {
        SessionInfo session = findSession(id);
        if (session == null) return;
        activeSessionId = id;
        writeActiveSessionId();
    }

    public String getActiveSessionId() {
        return activeSessionId;
    }

    public boolean isSessionResumed(String sessionId) {
        return resumedSessions.contains(sessionId);
    }

    public synchronized void markSessionResumed(String sessionId) {
        SessionInfo session = findSession(sessionId);
        if (session != null && !session.isResumed()) {
            session.setResumed(true);
            resumedSessions.add(sessionId);
            saveSessions();
        }
    }

    /**
     * 根据会话id 查找对应的会话
     * @param id
     * @return
     */
    private SessionInfo findSession(String id) {
        return sessions.stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null);
    }

    /**
     * 将活跃会话id 写入文件中 ， 发送的对话 进入的是这个会话
     */
    private void writeActiveSessionId() {
        try {
            Files.writeString(sessionIdFile, activeSessionId);
        } catch (IOException e) {
            log.error("写入活跃会话文件失败", e);
        }
    }

    private void loadSessions() {
        try {
            byte[] data = Files.readAllBytes(sessionsFile);
            sessions = objectMapper.readValue(data, new TypeReference<List<SessionInfo>>() {});
        } catch (IOException e) {
            log.error("加载会话列表失败", e);
            sessions = new ArrayList<>();
        }
    }

    /**
     * 将会话文件 保存
     */
    private void saveSessions() {
        try {
            objectMapper.writeValue(sessionsFile.toFile(), sessions);
        } catch (IOException e) {
            log.error("保存会话列表失败", e);
        }
    }
}
