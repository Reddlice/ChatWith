package org.reddlice.chatwith.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName: DefaultSettingConfig
 * @description: 设置配置类
 * @version: 1.0
 * @author: Redd_ice
 * @Date: 26/4/2026 下午 10:36
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "setting")
public class DefaultSettingConfig {
    private String skillName;

    private String systemPrompt;

}
