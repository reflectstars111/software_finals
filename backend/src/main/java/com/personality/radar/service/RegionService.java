package com.personality.radar.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personality.radar.common.BusinessException;
import com.personality.radar.domain.UserAccount;
import com.personality.radar.domain.UserRegion;
import com.personality.radar.dto.ApiDtos;
import com.personality.radar.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.util.List;

@Service
public class RegionService {
    private final ProvinceRepository provinces;
    private final CityRepository cities;
    private final DistrictRepository districts;
    private final UserRegionRepository userRegions;

    public RegionService(ProvinceRepository provinces, CityRepository cities,
                         DistrictRepository districts, UserRegionRepository userRegions) {
        this.provinces = provinces;
        this.cities = cities;
        this.districts = districts;
        this.userRegions = userRegions;
    }

    public List<ApiDtos.SimpleRegion> listProvinces() {
        return provinces.findAll().stream()
                .map(p -> new ApiDtos.SimpleRegion(p.getId(), p.getName()))
                .toList();
    }

    public List<ApiDtos.SimpleRegion> listCities(Long provinceId) {
        return cities.findByProvinceIdOrderByName(provinceId).stream()
                .map(c -> new ApiDtos.SimpleRegion(c.getId(), c.getName()))
                .toList();
    }

    public List<ApiDtos.SimpleRegion> listDistricts(Long cityId) {
        return districts.findByCityIdOrderByName(cityId).stream()
                .map(d -> new ApiDtos.SimpleRegion(d.getId(), d.getName()))
                .toList();
    }

    public ApiDtos.RegionResponse getCurrent(UserAccount user) {
        return userRegions.findByUserAndIsCurrentTrue(user)
                .map(r -> new ApiDtos.RegionResponse(r.getProvince(), r.getCity(), r.getDistrict()))
                .orElse(null);
    }

    @Transactional
    public ApiDtos.RegionResponse save(UserAccount user, String province, String city, String district) {
        userRegions.findByUserAndIsCurrentTrue(user).ifPresent(r -> {
            r.setCurrent(false);
            userRegions.save(r);
        });
        UserRegion region = new UserRegion();
        region.setUser(user);
        region.setProvince(province);
        region.setCity(city);
        region.setDistrict(district != null ? district : "");
        region.setCurrent(true);
        userRegions.save(region);
        return new ApiDtos.RegionResponse(province, city, district != null ? district : "");
    }

    public List<ApiDtos.RegionRecord> history(UserAccount user) {
        return userRegions.findByUserOrderByCreatedAtDesc(user).stream()
                .map(r -> new ApiDtos.RegionRecord(r.getProvince(), r.getCity(), r.getDistrict(),
                        r.isCurrent(), r.getCreatedAt()))
                .toList();
    }

    public ApiDtos.RegionResponse reverseGeocode(double lat, double lng) {
        try {
            RestTemplate rt = new RestTemplate();
            // Use ip-api.com reverse geocode (same service as IP locate, China-friendly)
            String url = String.format("http://ip-api.com/json/?lat=%f&lon=%f&lang=zh-CN&fields=city,regionName", lat, lng);
            String json = rt.getForObject(url, String.class);
            if (json == null) return null;
            JsonNode node = new ObjectMapper().readTree(json);
            String province = node.path("regionName").asText();
            String city = node.path("city").asText();
            if (province.isEmpty() || city.isEmpty()) return null;
            return new ApiDtos.RegionResponse(province, city, "");
        } catch (Exception e) {
            return null;
        }
    }

    public ApiDtos.RegionResponse locateByIp(String ip) {
        try {
            RestTemplate rt = new RestTemplate();
            String url = "http://ip-api.com/json/" + ip + "?lang=zh-CN&fields=city,regionName,country";
            String json = rt.getForObject(url, String.class);
            if (json == null) return null;
            JsonNode node = new ObjectMapper().readTree(json);
            String province = node.path("regionName").asText();
            String city = node.path("city").asText();
            if (province.isEmpty() || city.isEmpty()) return null;
            return new ApiDtos.RegionResponse(province, city, "");
        } catch (Exception e) {
            return null;
        }
    }
}
