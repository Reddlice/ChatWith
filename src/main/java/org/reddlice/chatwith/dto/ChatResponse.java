package org.reddlice.chatwith.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 返回类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {

    private String reply;
    private String skillUsed;
    private boolean success;

}
