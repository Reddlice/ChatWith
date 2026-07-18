package org.reddlice.chatwith.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatLogRequest {
    private String sessionId;
    private String sessionName;
    private String createdAt;
    private List<Map<String, String>> messages;
}
