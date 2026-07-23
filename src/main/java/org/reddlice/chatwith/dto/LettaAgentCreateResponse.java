package org.reddlice.chatwith.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LettaAgentCreateResponse {

    private String id;

    private String name;

    private String system;

    private String model;

    private String embedding;

    @JsonProperty("agent_type")
    private String agentType;

    @JsonProperty("created_at")
    private String createdAt;
}
