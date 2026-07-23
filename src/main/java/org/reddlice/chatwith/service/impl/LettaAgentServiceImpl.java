package org.reddlice.chatwith.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reddlice.chatwith.config.LettaServerConfig;
import org.reddlice.chatwith.dto.LettaAgentCreateRequest;
import org.reddlice.chatwith.dto.LettaAgentCreateResponse;
import org.reddlice.chatwith.service.LettaAgentService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LettaAgentServiceImpl implements LettaAgentService {

    private final LettaServerConfig lettaServerConfig;
    private final RestTemplate restTemplate;

    @Override
    public LettaAgentCreateResponse createAgent(LettaAgentCreateRequest request) {
        String url = lettaServerConfig.getUrl() + "/v1/agents/";

        String system = request.getSystem() != null && !request.getSystem().isBlank()
                ? request.getSystem()
                : lettaServerConfig.getSystemPrompt();

        Map<String, Object> body = Map.of(
                "name", request.getName() != null ? request.getName() : "",
                "system", system != null ? system : "",
                "model", lettaServerConfig.getModel(),
                "embedding", lettaServerConfig.getEmbedding()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        log.info("创建 Letta Agent: name={}, model={}", request.getName(), lettaServerConfig.getModel());

        try {
            ResponseEntity<LettaAgentCreateResponse> response = restTemplate.postForEntity(
                    url, entity, LettaAgentCreateResponse.class);

            if (response.getBody() == null) {
                throw new RuntimeException("Letta Server 返回空响应");
            }

            log.info("Letta Agent 创建成功: id={}", response.getBody().getId());
            return response.getBody();

        } catch (RestClientException e) {
            log.error("调用 Letta Server 创建 Agent 失败: {}", e.getMessage(), e);
            throw new RuntimeException("Letta Server 连接失败: " + e.getMessage(), e);
        }
    }

}
