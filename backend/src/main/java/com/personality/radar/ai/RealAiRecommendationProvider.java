package com.personality.radar.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personality.radar.ai.client.DeepSeekClient;
import com.personality.radar.ai.client.MapClient;
import com.personality.radar.ai.dto.RestaurantCandidate;
import com.personality.radar.domain.PersonalityDimension;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 把 C 上传的两个客户端（MapClient + DeepSeekClient）包成 B 约定的 {@link AiRecommendationProvider}。
 *
 * 它补上了 C 那版缺失的两件事：
 *   1) 个性化——把用户 10 维画像写进 Prompt（C 的入口只有经纬度，完全没用画像）；
 *   2) 输出形状——按 B↔C 契约返回 {@link AiRecoItem}（含 matchScore / reason / mapUrl）。
 * 防幻觉过滤沿用 C 的思路：只保留候选名单里真实存在的店。
 *
 * 只在 app.ai.provider=real 时生效；mock 模式下不创建本 bean，仍由 MockAiRecommendationProvider 顶替。
 * 失败一律抛异常，交给 B 的 AiRecommendationService 去降级到规则推荐。
 */
@Component
@ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "real")
public class RealAiRecommendationProvider implements AiRecommendationProvider {
    private static final Logger log = LoggerFactory.getLogger(RealAiRecommendationProvider.class);

    private final MapClient mapClient;
    private final DeepSeekClient deepSeekClient;
    private final ObjectMapper mapper;

    public RealAiRecommendationProvider(MapClient mapClient, DeepSeekClient deepSeekClient, ObjectMapper mapper) {
        this.mapClient = mapClient;
        this.deepSeekClient = deepSeekClient;
        this.mapper = mapper;
    }

    @Override
    public List<AiRecoItem> recommend(AiRecoContext context) {
        if (context.lat() == null || context.lng() == null) {
            // C 的地图客户端是基于经纬度的；没有坐标就让上层降级
            throw new IllegalStateException("缺少经纬度，real provider 无法调用地图");
        }

        // 1) C 的地图客户端拉真实候选
        List<RestaurantCandidate> candidates = mapClient.nearbyRestaurants(context.lat(), context.lng());
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalStateException("地图未返回任何候选餐厅");
        }

        // 2) 带画像的 Prompt（这是相对 C 那版的关键增强）
        String prompt = buildPrompt(context.profileScores(), candidates);

        // 3) C 的大模型客户端
        String raw = deepSeekClient.chat(prompt);

        // 4) 解析 + 防幻觉过滤
        List<AiRecoItem> ranked = parseAndFilter(raw, candidates, context.limit());
        if (ranked.isEmpty()) {
            throw new IllegalStateException("AI 结果解析后为空（可能格式不符或全部被幻觉过滤）");
        }
        return ranked;
    }

    @Override
    public String name() {
        return "amap+deepseek";
    }

    // ---------- Prompt ----------

    private String buildPrompt(Map<String, Integer> scores, List<RestaurantCandidate> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是本地餐厅个性化推荐助手。请结合【用户画像】对【候选餐厅】排序，并给出简短推荐理由。\n\n");

        sb.append("【用户画像】（0-100，越高越强）：\n");
        sb.append(profileLine(scores, PersonalityDimension.FOOD_ADVENTURE, "饮食探索（高=爱尝新，低=偏好熟悉稳定）"));
        sb.append(profileLine(scores, PersonalityDimension.FOOD_SOCIAL, "饮食社交（高=爱聚餐分享，低=偏好安静独食）"));
        sb.append(profileLine(scores, PersonalityDimension.EXTRAVERSION, "外向性"));
        sb.append(profileLine(scores, PersonalityDimension.OPENNESS, "开放性"));
        sb.append("\n");

        sb.append("【候选餐厅】（只能从这里面选，名字必须原样照抄）：\n");
        for (int i = 0; i < list.size(); i++) {
            RestaurantCandidate r = list.get(i);
            sb.append(i + 1).append(". ").append(r.getName())
                    .append(" | ").append(r.getAddress())
                    .append(" | 距离").append(r.getDistance()).append("m\n");
        }

        sb.append("""

                你是 API 系统，只能输出 JSON，禁止任何解释、Markdown、```json 包裹或多余文字。
                要求：
                - 只能从候选餐厅里选，name 必须与候选列表中的名字完全一致；
                - 按与用户画像的契合度从高到低排序；
                - score 为 0-100 的契合度；reason 用一句话（不超过 40 字）说明为什么适合这个用户。

                必须严格输出：
                {"results":[{"name":"餐厅名","score":88,"reason":"推荐理由"}]}
                """);
        return sb.toString();
    }

    private String profileLine(Map<String, Integer> scores, PersonalityDimension dim, String desc) {
        int v = scores.getOrDefault(dim.name(), 50);
        return "- " + desc + "：" + v + "\n";
    }

    // ---------- 解析 + 过滤 ----------

    private List<AiRecoItem> parseAndFilter(String raw, List<RestaurantCandidate> candidates, int limit) {
        String cleaned = raw == null ? "" : raw.trim();
        if (cleaned.contains("```")) {
            cleaned = cleaned.replace("```json", "").replace("```", "").trim();
        }

        // 候选按名字建索引，用于防幻觉过滤
        Map<String, RestaurantCandidate> byName = new LinkedHashMap<>();
        for (RestaurantCandidate c : candidates) {
            byName.putIfAbsent(c.getName(), c);
        }

        List<AiRecoItem> result = new ArrayList<>();
        try {
            JsonNode root = mapper.readTree(cleaned);
            JsonNode results = root.get("results");
            if (results == null || !results.isArray()) {
                return result;
            }
            for (JsonNode node : results) {
                String name = text(node, "name");
                if (name == null || !byName.containsKey(name)) {
                    continue; // 幻觉：候选里没有，丢弃
                }
                RestaurantCandidate c = byName.get(name);
                int score = clamp(node.has("score") ? node.get("score").asInt(60) : 60);
                String reason = text(node, "reason");
                result.add(new AiRecoItem(
                        c.getName(),
                        null,                       // category：高德 v5 基础返回里没有，留空
                        c.getAddress(),
                        c.getDistance(),
                        null,                       // rating：同上，C 的 MapClient 未取
                        null,                       // priceLevel：同上
                        score,
                        reason,
                        null,                       // tags：暂无
                        buildMapUrl(c),
                        c.getLocation()));          // externalId 用高德坐标，便于去重/排错
                if (result.size() >= Math.max(1, limit)) {
                    break;
                }
            }
        } catch (Exception e) {
            log.warn("解析 AI 输出失败: {}", e.toString());
        }
        return result;
    }

    private String buildMapUrl(RestaurantCandidate c) {
        if (c.getLocation() == null || c.getLocation().isBlank()) {
            return null;
        }
        // 高德 location 形如 "经度,纬度"，正好是 uri.amap.com 需要的 position 格式
        String name = c.getName() == null ? "" : URLEncoder.encode(c.getName(), StandardCharsets.UTF_8);
        return "https://uri.amap.com/marker?position=" + c.getLocation() + "&name=" + name;
    }

    private String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }

    private int clamp(int v) {
        return Math.max(0, Math.min(100, v));
    }
}