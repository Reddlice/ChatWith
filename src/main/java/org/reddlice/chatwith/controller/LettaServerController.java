package org.reddlice.chatwith.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reddlice.chatwith.dto.LettaAgentCreateRequest;
import org.reddlice.chatwith.dto.LettaAgentCreateResponse;
import org.reddlice.chatwith.service.LettaAgentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class LettaServerController {

    private final LettaAgentService lettaAgentService;

    @PostMapping("/chat-with/letta/agents")
    public LettaAgentCreateResponse createAgent(@RequestBody LettaAgentCreateRequest request) {
        log.info("创建 Agent 请求: name={}", request.getName());
        return lettaAgentService.createAgent(request);
    }
}
