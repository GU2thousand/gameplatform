package com.gamingplatform.ai.impl;

final class LangChain4jJsonSupport {

    private LangChain4jJsonSupport() {
    }

    static String extractJsonObject(String raw) {
        if (raw == null || raw.isBlank()) {
            return raw;
        }

        String text = raw.trim();
        if (text.startsWith("```")) {
            int firstLineEnd = text.indexOf('\n');
            if (firstLineEnd > 0) {
                text = text.substring(firstLineEnd + 1);
            }
            int lastFence = text.lastIndexOf("```");
            if (lastFence >= 0) {
                text = text.substring(0, lastFence);
            }
            text = text.trim();
        }

        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }

        return text;
    }
}
