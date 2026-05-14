package org.reddlice.chatwith.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionInfo {
    private String id;
    private String name;
    private String createdAt;
    private boolean resumed;
}
