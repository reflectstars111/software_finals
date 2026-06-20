package com.personality.radar.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personality.radar.ai.dto.RestaurantCandidate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
public class MapClient {

    @Value("${ai.amap.key}")
    private String amapKey;

    private final RestTemplate restTemplate =
            new RestTemplate();

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    public List<RestaurantCandidate> nearbyRestaurants(
            double latitude,
            double longitude) {

        try {

            String url =
                    "https://restapi.amap.com/v5/place/around"
                            + "?key=" + amapKey
                            + "&location=" + longitude + "," + latitude
                            + "&keywords=餐厅"
                            + "&radius=5000";

            String json =
                    restTemplate.getForObject(
                            url,
                            String.class);

            JsonNode root =
                    objectMapper.readTree(json);

            JsonNode pois =
                    root.get("pois");

            List<RestaurantCandidate> result =
                    new ArrayList<>();

            for (JsonNode poi : pois) {

                RestaurantCandidate item =
                        new RestaurantCandidate();

                item.setName(
                        poi.get("name").asText());

                item.setAddress(
                        poi.get("address").asText());

                item.setDistance(
                        poi.get("distance").asDouble());
                
                item.setLocation(
                    poi.get("location").asText());

                result.add(item);
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

