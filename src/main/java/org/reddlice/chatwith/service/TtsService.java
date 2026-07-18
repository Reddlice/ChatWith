package org.reddlice.chatwith.service;

public interface TtsService {
    byte[] textToSpeech(String text);

    void textToSpeechAsync(String text, String messageId, String sessionId);
}
