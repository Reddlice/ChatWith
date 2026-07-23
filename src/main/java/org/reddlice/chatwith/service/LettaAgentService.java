package org.reddlice.chatwith.service;

import org.reddlice.chatwith.dto.LettaAgentCreateRequest;
import org.reddlice.chatwith.dto.LettaAgentCreateResponse;

public interface LettaAgentService {

    /**
     * 创建 Letta Agent
     * @param request 包含 name 和 system 提示词
     * @return 创建好的 Agent 信息
     */
    LettaAgentCreateResponse createAgent(LettaAgentCreateRequest request);

}
