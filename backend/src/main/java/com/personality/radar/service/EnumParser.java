package com.personality.radar.service;

import com.personality.radar.common.BusinessException;
import com.personality.radar.domain.FeedbackRating;
import com.personality.radar.domain.SceneType;
import com.personality.radar.domain.TestType;

public final class EnumParser {
    private EnumParser() {
    }

    public static TestType testType(String value) {
        return parse(TestType.class, value, "测试类型不存在");
    }

    public static SceneType sceneType(String value) {
        return parse(SceneType.class, value, "推荐场景不存在");
    }

    public static FeedbackRating rating(String value) {
        return switch (value.toLowerCase()) {
            case "like", "喜欢" -> FeedbackRating.LIKE;
            case "neutral", "一般" -> FeedbackRating.NEUTRAL;
            case "dislike", "不喜欢" -> FeedbackRating.DISLIKE;
            default -> throw new BusinessException(400, "评价类型不正确");
        };
    }

    private static <E extends Enum<E>> E parse(Class<E> type, String value, String message) {
        try {
            return Enum.valueOf(type, value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(400, message);
        }
    }
}

