package com.personality.radar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

/**
 * 【A 和 B 之间的契约】前端拿到/提交的就是这些结构。
 * 单独成文件，避免和 ApiDtos.java（C/A 也会动）产生 merge 冲突。
 */
public final class AiDtos {
    private AiDtos() {
    }

    /** 单条 AI 推荐结果（去掉了 externalId 这类内部字段，只留前端要展示的）。 */
    public record AiRecommendationItemResponse(
            String name,
            String category,
            String address,
            Double distanceMeters,
            Double rating,
            String priceLevel,
            int matchScore,
            String reason,
            List<String> tags,
            String mapUrl) {
    }

    /**
     * 一次 AI 推荐的完整响应。
     *
     * @param recordId    本次推荐的持久化记录 id，前端提交反馈时回传它
     * @param source      "AI"（C 的真实结果）或 "RULE_FALLBACK"（降级到规则推荐）
     * @param degraded    是否降级（true 时前端可提示「AI 暂时不可用，已为你展示常规推荐」）
     */
    public record AiRecommendationResponse(
            Long recordId,
            String scene,
            String source,
            boolean degraded,
            String city,
            Instant generatedAt,
            List<AiRecommendationItemResponse> items) {
    }

    /** AI 推荐的反馈（针对某一条结果，用 itemName 定位）。 */
    public record AiFeedbackRequest(
            @NotBlank String rating,
            @Size(max = 100) String comment,
            @Size(max = 120) String itemName) {
    }

    /** 历史记录里的一条（精简）。 */
    public record AiRecordSummaryResponse(
            Long recordId,
            String scene,
            String source,
            String city,
            int itemCount,
            Instant createdAt) {
    }
}