package com.ani.anitrack.service;

import com.ani.anitrack.entity.Checkin;
import com.ani.anitrack.mapper.CheckinMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CheckinService {

    private final CheckinMapper checkinMapper;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public CheckinService(CheckinMapper checkinMapper, StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.checkinMapper = checkinMapper;
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    public Map<String, Integer> getHeatmap(Integer userId) {
        if (userId == null) return new LinkedHashMap<>();
        String key = "heatmap:" + userId;
        try {
            String cached = redis.opsForValue().get(key);
            if (cached != null) {
                try {
                    return objectMapper.readValue(cached, new TypeReference<Map<String, Integer>>() {});
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            // Redis unavailable, fall through to DB query
        }
        List<Checkin> list = checkinMapper.selectByUserId(userId);
        Map<String, Integer> result = new LinkedHashMap<>();
        for (Checkin c : list) {
            result.put(c.getCheckDate(), c.getCount());
        }
        try {
            redis.opsForValue().set(key, objectMapper.writeValueAsString(result), Duration.ofHours(1));
        } catch (Exception ignored) {
        }
        return result;
    }

    public void recordCheckin(Integer userId) {
        checkinMapper.recordCheckin(userId);
        redis.delete("heatmap:" + userId);
    }
}
