package com.personality.radar.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personality.radar.ai.client.DeepSeekClient;
import com.personality.radar.ai.data.CityRepository;
import com.personality.radar.ai.dto.CityCandidate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CityRecommendationService {

    private final DeepSeekClient deepSeekClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CityRecommendationService(DeepSeekClient deepSeekClient) {
        this.deepSeekClient = deepSeekClient;
    }

    public List<CityCandidate> recommend(
            double openness,
            double conscientiousness,
            double extraversion,
            double agreeableness,
            double emotionalStability,
            double foodExplore,
            double foodSocial,
            double travelExplore,
            double travelPlan,
            double socialEnergy
    ) {

        // 1. 城市候选池
        List<String> cities = CityRepository.getCities();

        // 2. 生成Prompt
        String prompt = buildPrompt(
                cities,
                openness,
                conscientiousness,
                extraversion,
                agreeableness,
                emotionalStability,
                foodExplore,
                foodSocial,
                travelExplore,
                travelPlan,
                socialEnergy
        );

        // 3. 调用AI
        String result = deepSeekClient.chat(prompt);

        System.out.println("=== CITY AI OUTPUT ===");
        System.out.println(result);

        try {
            Map<String, Object> map =
                    objectMapper.readValue(result, Map.class);

            List<Map<String, Object>> results =
                    (List<Map<String, Object>>) map.get("results");

            List<String> ordered = results.stream()
                    .map(r -> (String) r.get("name"))
                    .toList();

            Map<String, CityCandidate> cityMap =
                    cities.stream()
                            .collect(Collectors.toMap(
                                    c -> c,
                                    c -> {
                                        CityCandidate cc = new CityCandidate();
                                        cc.setName(c);
                                        return cc;
                                    }
                            ));

            List<CityCandidate> ranked = new ArrayList<>();

            for (String name : ordered) {
                if (cityMap.containsKey(name)) {
                    ranked.add(cityMap.get(name));
                }
            }

            return ranked.isEmpty()
                    ? cities.stream().map(c -> {
                        CityCandidate cc = new CityCandidate();
                        cc.setName(c);
                        return cc;
                    }).toList()
                    : ranked;

        } catch (Exception e) {
            e.printStackTrace();

            return cities.stream().map(c -> {
                CityCandidate cc = new CityCandidate();
                cc.setName(c);
                return cc;
            }).toList();
        }
    }

    private String buildPrompt(
            List<String> cities,
            double openness,
            double conscientiousness,
            double extraversion,
            double agreeableness,
            double emotionalStability,
            double foodExplore,
            double foodSocial,
            double travelExplore,
            double travelPlan,
            double socialEnergy
    ) {

        StringBuilder sb = new StringBuilder();

        sb.append("你是一个旅游城市推荐系统。\n");
        sb.append("根据用户人格与偏好对城市进行排序。\n");
        sb.append("只能从给定城市中选择，禁止编造城市。\n\n");

        sb.append("用户画像：\n");
        sb.append("openness=").append(openness).append("\n");
        sb.append("conscientiousness=").append(conscientiousness).append("\n");
        sb.append("extraversion=").append(extraversion).append("\n");
        sb.append("agreeableness=").append(agreeableness).append("\n");
        sb.append("emotionalStability=").append(emotionalStability).append("\n");
        sb.append("foodExplore=").append(foodExplore).append("\n");
        sb.append("foodSocial=").append(foodSocial).append("\n");
        sb.append("travelExplore=").append(travelExplore).append("\n");
        sb.append("travelPlan=").append(travelPlan).append("\n");
        sb.append("socialEnergy=").append(socialEnergy).append("\n\n");

        sb.append("城市列表：\n");
        for (String c : cities) {
            sb.append("- ").append(c).append("\n");
        }

        sb.append("\n只返回JSON格式：\n");
        sb.append("{\"results\":[{\"name\":\"城市名\"}]}");

        return sb.toString();
    }
}