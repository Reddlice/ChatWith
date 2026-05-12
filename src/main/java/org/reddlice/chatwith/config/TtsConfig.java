package org.reddlice.chatwith.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "tts")
@Data
public class TtsConfig {
    private String url;
    private String refAudioPath;
    private String promptText;
    private String promptLang;
    private String textLang;
    private String mediaType;
    private float speedFactor;
}
