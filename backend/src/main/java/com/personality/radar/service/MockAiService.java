package com.personality.radar.service;

import com.personality.radar.domain.PersonalityDimension;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.springframework.stereotype.Service;

@Service
public class MockAiService {

    public Map<String, Integer> generateVector(String content) {
        int hash = Math.abs(content.hashCode());
        Random rng = new Random(hash);
        Map<String, Integer> vector = new HashMap<>();
        for (PersonalityDimension dim : PersonalityDimension.values()) {
            vector.put(dim.name(), rng.nextInt(101)); // [0, 100]
        }
        return vector;
    }

    public String reviewContent(String content) {
        // MVP: always approve
        return "APPROVED";
    }
}
