package org.reddlice.chatwith.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @ClassName: ClaudeCodeConfig
 * @description: ClaudeCode 配置类
 * @version: 1.0
 * @author: Redd_ice
 * @Date: 26/4/2026 下午 8:54
 */
@Configuration
@ConfigurationProperties(prefix = "claude-code")
@Data
public class ClaudeCodeConfig {
    /**
     * claude code 路径
     */
    private String path;

    /**
     * 超时时间
     */
    private Integer timeout;

    /**
     * skill dir
     */
    private String skillsDir;



}
