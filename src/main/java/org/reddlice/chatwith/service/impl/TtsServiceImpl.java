package org.reddlice.chatwith.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reddlice.chatwith.component.AudioStore;
import org.reddlice.chatwith.config.TtsConfig;
import org.reddlice.chatwith.service.TranslateService;
import org.reddlice.chatwith.service.TtsService;
import org.reddlice.chatwith.util.TextCleaner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TtsServiceImpl implements TtsService {

    private final TtsConfig ttsConfig;
    private final TranslateService translateService;
    private final ObjectMapper objectMapper;
    private final AudioStore audioStore;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    @Override
    public byte[] textToSpeech(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            String cleaned = TextCleaner.cleanForTts(text);
            log.info("TTS 清洗: {} 字符 → {} 字符", text.length(), cleaned.length());
            log.info("清洗后的字符:{}",cleaned);
            String translated = translateService.toJapanese(cleaned);
            log.info("TTS 翻译: {} 字符 → {} 字符", cleaned.length(), translated.length());
            String json = objectMapper.writeValueAsString(Map.of(
                    "text", translated,
                    "text_lang", "ja",
                    "ref_audio_path", ttsConfig.getRefAudioPath(),
                    "prompt_text", ttsConfig.getPromptText(),
                    "prompt_lang", ttsConfig.getPromptLang(),
                    "media_type", ttsConfig.getMediaType(),
                    "speed_factor", ttsConfig.getSpeedFactor()
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ttsConfig.getUrl() + "/tts"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<byte[]> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (resp.statusCode() == 200) {
                return resp.body();
            }
            log.warn("TTS API 返回非 200: {} body={}", resp.statusCode(), new String(resp.body()));
        } catch (Exception e) {
            log.warn("TTS 调用失败，降级为纯文本", e);
        }
        return null;
    }

    @Async
    @Override
    public void textToSpeechAsync(String text, String messageId) {
        log.info("异步 TTS 开始: messageId={}", messageId);
        byte[] audio = textToSpeech(text);
        if (audio != null && audio.length > 0) {
            audioStore.put(messageId, Base64.getEncoder().encodeToString(audio));
            log.info("异步 TTS 完成: messageId={}, size={}bytes", messageId, audio.length);
        } else {
            log.warn("异步 TTS 失败: messageId={}", messageId);
        }
    }
}
