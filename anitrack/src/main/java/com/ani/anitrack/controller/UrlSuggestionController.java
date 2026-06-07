package com.ani.anitrack.controller;

import com.ani.anitrack.entity.UrlSuggestion;
import com.ani.anitrack.service.UrlSuggestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "网址建议")
@RestController
@RequestMapping("/api/url-suggestions")
public class UrlSuggestionController {

    private final UrlSuggestionService service;

    public UrlSuggestionController(UrlSuggestionService service) {
        this.service = service;
    }

    private Integer userId(HttpServletRequest request) {
        return (Integer) request.getAttribute("userId");
    }

    @Operation(summary = "获取网址建议列表")
    @GetMapping
    public ResponseEntity<List<UrlSuggestion>> list(HttpServletRequest request) {
        return ResponseEntity.ok(service.findAllVisible(userId(request)));
    }

    @Operation(summary = "添加自定义网址")
    @PostMapping
    public ResponseEntity<UrlSuggestion> add(@RequestBody UrlSuggestion suggestion, HttpServletRequest request) {
        suggestion.setUserId(userId(request));
        return new ResponseEntity<>(service.addCustom(suggestion), HttpStatus.CREATED);
    }

    @Operation(summary = "删除自定义网址")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id, HttpServletRequest request) {
        boolean deleted = service.deleteCustom(id, userId(request));
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @Operation(summary = "切换置顶状态")
    @PutMapping("/{id}/pin")
    public ResponseEntity<Void> togglePin(@PathVariable Integer id) {
        service.togglePin(id);
        return ResponseEntity.ok().build();
    }
}
