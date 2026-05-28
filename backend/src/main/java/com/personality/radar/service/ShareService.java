package com.personality.radar.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personality.radar.common.BusinessException;
import com.personality.radar.domain.ShareLink;
import com.personality.radar.domain.UserAccount;
import com.personality.radar.dto.ApiDtos;
import com.personality.radar.repository.ShareLinkRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShareService {
    private final ReportService reportService;
    private final ShareLinkRepository links;
    private final ObjectMapper mapper;
    private final String publicBaseUrl;
    private final SecureRandom random = new SecureRandom();

    public ShareService(
            ReportService reportService,
            ShareLinkRepository links,
            ObjectMapper mapper,
            @Value("${app.public-base-url}") String publicBaseUrl) {
        this.reportService = reportService;
        this.links = links;
        this.mapper = mapper;
        this.publicBaseUrl = publicBaseUrl;
    }

    @Transactional
    public ApiDtos.ShareResponse create(UserAccount user) {
        ApiDtos.ReportResponse report = reportService.reportFor(user);
        String token = token();
        ShareLink link = new ShareLink();
        link.setOwner(user);
        link.setToken(token);
        link.setExpiresAt(Instant.now().plus(90, ChronoUnit.DAYS));
        try {
            link.setReportJson(mapper.writeValueAsString(report));
        } catch (JsonProcessingException ex) {
            throw new BusinessException(500, "分享报告生成失败");
        }
        links.save(link);
        return new ApiDtos.ShareResponse(token, publicBaseUrl + "/share/" + token, report);
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.ShareLinkSummaryResponse> list(UserAccount user) {
        return links.findByOwnerOrderByCreatedAtDesc(user).stream()
                .map(this::summary)
                .toList();
    }

    @Transactional
    public void revoke(UserAccount user, Long id) {
        ShareLink link = links.findByIdAndOwner(id, user)
                .orElseThrow(() -> new BusinessException(404, "分享链接不存在"));
        link.setActive(false);
        link.setRevokedAt(Instant.now());
    }

    public ApiDtos.ReportResponse get(String token) {
        ShareLink link = links.findByToken(token).orElseThrow(() -> new BusinessException(404, "分享链接不存在"));
        if (!link.isActive() || (link.getExpiresAt() != null && link.getExpiresAt().isBefore(Instant.now()))) {
            throw new BusinessException(404, "分享链接不存在");
        }
        try {
            return mapper.readValue(link.getReportJson(), ApiDtos.ReportResponse.class);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(500, "分享报告读取失败");
        }
    }

    private String token() {
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private ApiDtos.ShareLinkSummaryResponse summary(ShareLink link) {
        return new ApiDtos.ShareLinkSummaryResponse(
                link.getId(),
                link.getToken(),
                publicBaseUrl + "/share/" + link.getToken(),
                link.isActive(),
                link.getCreatedAt(),
                link.getExpiresAt(),
                link.getRevokedAt());
    }
}
