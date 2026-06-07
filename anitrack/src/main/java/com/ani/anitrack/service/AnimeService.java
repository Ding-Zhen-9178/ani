package com.ani.anitrack.service;

import com.ani.anitrack.entity.Anime;
import com.ani.anitrack.mapper.AnimeMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Service
public class AnimeService {

    private final AnimeMapper animeMapper;
    private final CheckinService checkinService;
    private final StringRedisTemplate redis;

    public AnimeService(AnimeMapper animeMapper, CheckinService checkinService, StringRedisTemplate redis) {
        this.animeMapper = animeMapper;
        this.checkinService = checkinService;
        this.redis = redis;
    }

    public Anime addAnime(Anime anime) {
        if (anime.getLastWatchDate() == null) {
            anime.setLastWatchDate(LocalDate.now().toString());
        }
        if (anime.getStatus() == null) {
            anime.setStatus("watching");
        }
        capCurrentEp(anime);
        animeMapper.insert(anime);
        return anime;
    }

    public Anime updateAnime(Anime anime) {
        capCurrentEp(anime);
        animeMapper.update(anime);
        return anime;
    }

    private void capCurrentEp(Anime anime) {
        if (anime.getTotalEp() != null && anime.getCurrentEp() != null
                && anime.getCurrentEp() > anime.getTotalEp()) {
            anime.setCurrentEp(anime.getTotalEp());
        }
    }

    public boolean deleteAnime(Integer id, Integer userId) {
        return animeMapper.deleteById(id, userId) > 0;
    }

    public Anime getAnime(Integer id, Integer userId) {
        return animeMapper.selectById(id, userId);
    }

    public List<Anime> listAllOrderByUpdateDay(Integer userId) {
        return animeMapper.selectAllOrderByUpdateDay(userId);
    }

    public IncrementResult incrementEp(Integer id, Integer userId) {
        String rateKey = "rate:incr:" + userId;
        Boolean acquired = redis.opsForValue().setIfAbsent(rateKey, "1", Duration.ofSeconds(3));
        if (!Boolean.TRUE.equals(acquired)) {
            return IncrementResult.RATE_LIMITED;
        }
        int rows = animeMapper.incrementEp(id, userId);
        if (rows > 0) {
            checkinService.recordCheckin(userId);
            return IncrementResult.OK;
        }
        return IncrementResult.CAPPED;
    }

    public void markCompleted(Integer id, Integer userId) {
        animeMapper.markCompleted(id, userId);
    }

    public void updateStatus(Integer id, String status, Integer userId) {
        animeMapper.updateStatus(id, status, userId);
    }

    public enum IncrementResult {
        OK, CAPPED, RATE_LIMITED
    }
}
