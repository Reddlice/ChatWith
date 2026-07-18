package org.reddlice.chatwith.component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reddlice.chatwith.config.AudioConfig;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class AudioStore {

    private final AudioConfig audioConfig;

    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private final Map<String, String> msgToSession = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        File dir = new File(audioConfig.getStoreDir());
        if (!dir.exists()) {
            dir.mkdirs();
            log.info("音频存储目录已创建: {}", dir.getAbsolutePath());
        }
    }

    public void put(String sessionId, String messageId, String audioBase64) {
        cache.put(messageId, audioBase64);
        msgToSession.put(messageId, sessionId);
        writeToDisk(sessionId, messageId, audioBase64);
    }

    public String get(String messageId) {
        String cached = cache.get(messageId);
        if (cached != null) {
            return cached;
        }
        String sessionId = msgToSession.get(messageId);
        if (sessionId == null) {
            return null;
        }
        return readFromDisk(sessionId, messageId);
    }

    public Map<String, String> getBySession(String sessionId) {
        Map<String, String> result = new ConcurrentHashMap<>();
        // 从缓存中取属于该会话的音频
        for (Map.Entry<String, String> entry : cache.entrySet()) {
            if (sessionId.equals(msgToSession.get(entry.getKey()))) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        // 扫描磁盘补全缓存中缺失的
        Path sessionDir = Path.of(audioConfig.getStoreDir(), sessionId);
        if (Files.isDirectory(sessionDir)) {
            File[] files = sessionDir.toFile().listFiles((dir, name) -> name.endsWith(".wav"));
            if (files != null) {
                for (File file : files) {
                    String msgId = file.getName().replace(".wav", "");
                    if (!result.containsKey(msgId)) {
                        String base64 = readFromDisk(sessionId, msgId);
                        if (base64 != null) {
                            result.put(msgId, base64);
                            cache.put(msgId, base64);
                            msgToSession.put(msgId, sessionId);
                        }
                    }
                }
            }
        }
        return result;
    }

    private void writeToDisk(String sessionId, String messageId, String audioBase64) {
        try {
            Path sessionDir = Path.of(audioConfig.getStoreDir(), sessionId);
            Files.createDirectories(sessionDir);
            byte[] audioBytes = Base64.getDecoder().decode(audioBase64);
            Files.write(sessionDir.resolve(messageId + ".wav"), audioBytes);
        } catch (IOException e) {
            log.error("写入音频文件失败: {}/{}", sessionId, messageId, e);
        }
    }

    private String readFromDisk(String sessionId, String messageId) {
        try {
            Path file = Path.of(audioConfig.getStoreDir(), sessionId, messageId + ".wav");
            if (Files.exists(file)) {
                byte[] bytes = Files.readAllBytes(file);
                return Base64.getEncoder().encodeToString(bytes);
            }
        } catch (IOException e) {
            log.warn("读取音频文件失败: {}/{}", sessionId, messageId, e);
        }
        return null;
    }
}
