package org.reddlice.chatwith.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "letta-server")
public class LettaServerConfig {

    /**
     * 服务地址
     */
    private String url;

    /**
     * 默认超时时间
     */
    private int timeout;

    /**
     * llm 模型
     */
    private String model;

    /**
     * embedding 模型
     */
    private String embedding;

    /**
     * 系统提示词文件路径
     */
    private String systemPromptPath;

    /**
     * 系统提示词内容（从文件加载）
     */
    private String systemPrompt;

    @PostConstruct
    public void loadSystemPrompt() {
        if (systemPromptPath == null || systemPromptPath.isBlank()) {
            log.warn("未配置 letta-server.system-prompt-path，系统提示词为空");
            systemPrompt = "";
            return;
        }
        Path path = Paths.get(systemPromptPath);
        if (Files.isRegularFile(path)) {
            try {
                systemPrompt = Files.readString(path, Charset.defaultCharset());
                log.info("已加载 Letta 系统提示词: {} ({} 字符)", systemPromptPath, systemPrompt.length());
            } catch (Exception e) {
                log.error("读取系统提示词文件失败: {}", path, e);
                systemPrompt = "";
            }
        } else {
            log.warn("系统提示词文件不存在: {}", path.toAbsolutePath());
            systemPrompt = "";
        }
    }
}
