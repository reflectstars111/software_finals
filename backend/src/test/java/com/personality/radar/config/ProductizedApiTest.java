package com.personality.radar.config;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personality.radar.dto.ApiDtos;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProductizedApiTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void reportsCanBeStoredListedAndOpenedById() throws Exception {
        String token = login("13900000001", "User123456");

        mockMvc.perform(get("/api/reports/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.phone").value("13900000001"));

        String historyBody = mockMvc.perform(get("/api/reports/history").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long reportId = mapper.readTree(historyBody).path("data").path(0).path("id").asLong();

        mockMvc.perform(get("/api/reports/{id}", reportId).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(reportId))
                .andExpect(jsonPath("$.data.report.user.phone").value("13900000001"));
    }

    @Test
    void matchReportsCanBeListedAfterCreation() throws Exception {
        String token = login("13900000001", "User123456");

        mockMvc.perform(post("/api/matches")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"friendPhone\":\"13900000002\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.target.phone").value("13900000002"));

        mockMvc.perform(get("/api/matches").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data[0].target.phone").value("13900000002"));
    }

    @Test
    void shareLinksCanBeListedAndRevokedByOwner() throws Exception {
        String token = login("13900000001", "User123456");
        String created = mockMvc.perform(post("/api/shares/report").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isString())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode createdData = mapper.readTree(created).path("data");
        String shareToken = createdData.path("token").asText();

        String listBody = mockMvc.perform(get("/api/shares").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].token", hasItem(shareToken)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long shareId = mapper.readTree(listBody).path("data").path(0).path("id").asLong();

        mockMvc.perform(delete("/api/shares/{id}", shareId).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/shares/{token}", shareToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void adminDashboardAndUserStatusManagementAreAvailable() throws Exception {
        String adminToken = login("13800000000", "Admin@123456");

        mockMvc.perform(get("/api/admin/dashboard").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stats.users", greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.data.feedbackByRating").exists())
                .andExpect(jsonPath("$.data.testsByType").exists());

        String usersBody = mockMvc.perform(get("/api/admin/users").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].phone", hasItem("13900000001")))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode users = mapper.readTree(usersBody).path("data");
        long userId = 0;
        for (JsonNode user : users) {
            if ("13900000001".equals(user.path("phone").asText())) {
                userId = user.path("id").asLong();
            }
        }

        mockMvc.perform(put("/api/admin/users/{id}", userId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new ApiDtos.LoginRequest("13900000001", "User123456"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("账号已停用"));
    }

    @Test
    void recommendationRulesCanBeManagedByAdmin() throws Exception {
        String adminToken = login("13800000000", "Admin@123456");

        String created = mockMvc.perform(post("/api/admin/recommendation-rules")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tag\":\"quiet\",\"label\":\"安静偏好\",\"weight\":6,\"active\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tag").value("quiet"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long ruleId = mapper.readTree(created).path("data").path("id").asLong();

        mockMvc.perform(put("/api/admin/recommendation-rules/{id}", ruleId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tag\":\"quiet\",\"label\":\"安静恢复\",\"weight\":8,\"active\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.weight").value(8));

        mockMvc.perform(get("/api/admin/recommendation-rules").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].tag", hasItem("quiet")));

        mockMvc.perform(delete("/api/admin/recommendation-rules/{id}", ruleId).header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/recommendation-rules").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].tag", not(hasItem("quiet"))));
    }

    private String login(String phone, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new ApiDtos.LoginRequest(phone, password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return mapper.readTree(response).path("data").path("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
