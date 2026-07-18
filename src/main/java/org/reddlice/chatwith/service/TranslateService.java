package org.reddlice.chatwith.service;

public interface TranslateService {
    /**
     * 翻译为日语，失败返回原文
     */
    String toJapanese(String text);
}
