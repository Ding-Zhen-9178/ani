package com.ani.anitrack.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class BangumiService {

    private static final Logger log = LoggerFactory.getLogger(BangumiService.class);

    private final RestClient client = RestClient.builder()
            .defaultHeader("User-Agent", "anitrack/1.0")
            .build();

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> search(String keyword) {
        Map<String, Object> requestBody = Map.of(
                "keyword", keyword,
                "filter", Map.of("type", List.of(2)),
                "limit", 10
        );
        log.info("Bangumi search: keyword={}", keyword);
        Map<String, Object> body = client.post()
                .uri("https://api.bgm.tv/v0/search/subjects")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(Map.class);
        log.info("Bangumi response keys: {}", body != null ? body.keySet() : "null");
        if (body == null) return List.of();
        List<Map<String, Object>> data = (List<Map<String, Object>>) body.getOrDefault("data", List.of());
        log.info("Bangumi parsed {} results", data.size());
        return data;
    }
}