package com.personality.radar.service;

import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class SensitiveWordFilter {
    // Minimal set — expand in production
    private static final Set<String> WORDS = Set.of("违规词1", "违规词2");

    public boolean containsSensitiveWord(String text) {
        if (text == null || text.isBlank()) return false;
        String lower = text.toLowerCase();
        return WORDS.stream().anyMatch(lower::contains);
    }
}
