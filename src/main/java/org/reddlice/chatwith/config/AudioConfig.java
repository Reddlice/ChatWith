package org.reddlice.chatwith.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "audio")
@Data
public class AudioConfig {
    private String storeDir = "audio-store";
}
