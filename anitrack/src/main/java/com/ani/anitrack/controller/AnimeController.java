package com.ani.anitrack.controller;

import com.ani.anitrack.entity.Anime;
import com.ani.anitrack.service.AnimeService;
import com.ani.anitrack.service.AnimeService.IncrementResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "番剧管理")
@RestController
@RequestMapping("/api/anime")
public class AnimeController {

    private final AnimeService animeService;

    public AnimeController(AnimeService animeService) {
        this.animeService = animeService;
    }

    private Integer userId(HttpServletRequest request) {
        return (Integer) request.getAttribute("userId");
    }

    @Operation(summary = "添加番剧")
    @PostMapping
    public ResponseEntity<Anime> addAnime(@RequestBody Anime anime, HttpServletRequest request) {
        anime.setUserId(userId(request));
        Anime saved = animeService.addAnime(anime);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @Operation(summary = "更新番剧")
    @PutMapping
    public ResponseEntity<Anime> updateAnime(@RequestBody Anime anime, HttpServletRequest request) {
        anime.setUserId(userId(request));
        animeService.updateAnime(anime);
        return ResponseEntity.ok(anime);
    }

    @Operation(summary = "删除番剧")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnime(@PathVariable Integer id, HttpServletRequest request) {
        boolean deleted = animeService.deleteAnime(id, userId(request));
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @Operation(summary = "获取单个番剧")
    @GetMapping("/{id}")
    public ResponseEntity<Anime> getAnime(@PathVariable Integer id, HttpServletRequest request) {
        Anime anime = animeService.getAnime(id, userId(request));
        return anime != null ? ResponseEntity.ok(anime) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "获取番剧列表")
    @GetMapping("/list")
    public ResponseEntity<List<Anime>> listAnime(HttpServletRequest request) {
        return ResponseEntity.ok(animeService.listAllOrderByUpdateDay(userId(request)));
    }

    @Operation(summary = "观看集数+1")
    @PostMapping("/increment/{id}")
    public ResponseEntity<Void> incrementEp(@PathVariable Integer id, HttpServletRequest request) {
        IncrementResult result = animeService.incrementEp(id, userId(request));
        return switch (result) {
            case OK -> ResponseEntity.ok().build();
            case CAPPED -> ResponseEntity.status(HttpStatus.CONFLICT).build();
            case RATE_LIMITED -> ResponseEntity.status(429).build();
        };
    }

    @Operation(summary = "标记为已完结")
    @PostMapping("/complete/{id}")
    public ResponseEntity<Void> markCompleted(@PathVariable Integer id, HttpServletRequest request) {
        animeService.markCompleted(id, userId(request));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "更新追番状态")
    @PostMapping("/status/{id}")
    public ResponseEntity<Void> updateStatus(@PathVariable Integer id, @RequestParam String status,
                                             HttpServletRequest request) {
        animeService.updateStatus(id, status, userId(request));
        return ResponseEntity.ok().build();
    }
}
