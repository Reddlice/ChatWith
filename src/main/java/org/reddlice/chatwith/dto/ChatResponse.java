package org.reddlice.chatwith.dto;

import lombok.Data;

/**
 * 返回类
 */
@Data
public class ChatResponse {

    private String reply;
    private String skillUsed;
    private boolean success;
    private String messageId;
    private String audioBase64;

    public ChatResponse() {}

    public ChatResponse(String reply, String skillUsed, boolean success) {
        this.reply = reply;
        this.skillUsed = skillUsed;
        this.success = success;
    }

}
