package org.reddlice.chatwith.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "baidu.translate")
@Data
public class BaiduTranslateConfig {
    private String appId;
    private String secretKey;
    private String apiUrl;
}
