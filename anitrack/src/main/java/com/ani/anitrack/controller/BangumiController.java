package com.ani.anitrack.controller;

import com.ani.anitrack.service.BangumiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Bangumi搜索")
@RestController
@RequestMapping("/api/bangumi")
public class BangumiController {

    private static final Logger log = LoggerFactory.getLogger(BangumiController.class);
    private final BangumiService bangumiService;

    public BangumiController(BangumiService bangumiService) {
        this.bangumiService = bangumiService;
    }

    @Operation(summary = "搜索番剧")
    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String keyword) {
        try {
            List<Map<String, Object>> results = bangumiService.search(keyword);
            return ResponseEntity.ok(Map.of("data", results, "total", results.size()));
        } catch (Exception e) {
            log.error("Bangumi search failed for keyword: {}", keyword, e);
            String msg = e.getMessage();
            if (msg == null) msg = e.getClass().getSimpleName();
            return ResponseEntity.status(502).body(Map.of("error", "搜索服务暂不可用: " + msg));
        }
    }
}