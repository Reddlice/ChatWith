package org.reddlice.chatwith.util;

import java.util.regex.Pattern;

/**
 * 文本清洗工具 —— 移除括号及括号内容，为 TTS 语音合成准备干净口语文本
 */
public final class TextCleaner {

    private TextCleaner() {}

    /** 匹配中文全角括号（...） */
    private static final Pattern FULL_WIDTH = Pattern.compile("（[^）]*）");

    /** 匹配英文半角括号 (...) */
    private static final Pattern HALF_WIDTH = Pattern.compile("\\([^)]*\\)");

    /**
     * 移除所有括号及其中的内容（含全角/半角）
     */
    public static String removeParentheses(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        String result = FULL_WIDTH.matcher(text).replaceAll("");
        result = HALF_WIDTH.matcher(result).replaceAll("");
        // 清理多余空白行
        return result.replaceAll("(?m)^\\s*\\n", "").trim();
    }

    /**
     * 为 TTS 准备口语文本 —— 当前仅移除括号内容，后续可扩展更多清洗规则
     */
    public static String cleanForTts(String text) {
        return removeParentheses(text);
    }
}
