package org.reddlice.chatwith.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reddlice.chatwith.config.BaiduTranslateConfig;
import org.reddlice.chatwith.service.TranslateService;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslateServiceImpl implements TranslateService {

    private final BaiduTranslateConfig config;
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder().build();

    @Override
    public String toJapanese(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        try {
            String salt = String.valueOf(System.currentTimeMillis());
            String sign = md5(config.getAppId() + text + salt + config.getSecretKey());

            String body = "q=" + URLEncoder.encode(text, StandardCharsets.UTF_8)
                    + "&from=auto"
                    + "&to=jp"
                    + "&appid=" + config.getAppId()
                    + "&salt=" + salt
                    + "&sign=" + sign;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(config.getApiUrl()))
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(resp.body());
                JsonNode transResult = root.get("trans_result");
                if (transResult != null && transResult.isArray() && !transResult.isEmpty()) {
                    String dst = transResult.get(0).get("dst").asText();
                    log.info("翻译成功: {} 字符 → 日语: {} 字符", text.length(), dst.length());
                    log.info("翻译后的字符：{}",dst);
                    return dst;
                }
                log.warn("翻译响应无结果: {}", resp.body());
            } else {
                log.warn("翻译 API 返回非 200: {} body={}", resp.statusCode(), resp.body());
            }
        } catch (Exception e) {
            log.warn("翻译调用失败，降级为原文", e);
        }
        return text; // 降级返回原文
    }

    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(32);
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
