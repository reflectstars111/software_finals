package com.personality.radar.ai;

import java.util.List;

/**
 * B ↔ C 内部契约（出参，单条推荐）。
 * C 的真实实现：地图 POI 拿到真实餐厅 → LLM 排序+生成理由 → 组装成这个结构返回。
 *
 * 重要约束（C 实现时必须遵守，否则 B 这边的展示会缺字段）：
 * - name 必填、非空；
 * - matchScore 取值 0–100；
 * - reason 是给用户看的「为什么推荐给你」，不要超过 ~80 字；
 * - 列表里出现的店必须是地图候选里真实存在的，禁止 LLM 凭空编造（幻觉过滤是 C 的职责）。
 *
 * @param name          餐厅名（必填）
 * @param category      品类，如「日式拉面」，可空
 * @param address       地址，可空
 * @param distanceMeters 距离（米），可空
 * @param rating        评分，如 4.5，可空
 * @param priceLevel    人均，如「¥80/人」，可空
 * @param matchScore    AI 匹配度 0–100
 * @param reason        推荐理由（给用户看）
 * @param tags          标签，可空
 * @param mapUrl        地图跳转链接，可空
 * @param externalId    地图 POI id，用于去重/排错，可空
 */
public record AiRecoItem(
        String name,
        String category,
        String address,
        Double distanceMeters,
        Double rating,
        String priceLevel,
        int matchScore,
        String reason,
        List<String> tags,
        String mapUrl,
        String externalId) {
}