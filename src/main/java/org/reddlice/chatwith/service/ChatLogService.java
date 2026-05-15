package org.reddlice.chatwith.service;

import org.reddlice.chatwith.dto.ChatLogRequest;

public interface ChatLogService {
    void saveLog(ChatLogRequest request);
}
