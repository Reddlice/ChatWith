package org.reddlice.chatwith.component;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AudioStore {

    private final Map<String, String> store = new ConcurrentHashMap<>();

    public void put(String messageId, String audioBase64) {
        store.put(messageId, audioBase64);
    }

    public String get(String messageId) {
        return store.get(messageId);
    }
}
