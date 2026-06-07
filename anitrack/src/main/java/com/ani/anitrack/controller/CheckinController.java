package com.ani.anitrack.controller;

import com.ani.anitrack.service.CheckinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "签到管理")
@RestController
@RequestMapping("/api/checkin")
public class CheckinController {

    private final CheckinService checkinService;

    public CheckinController(CheckinService checkinService) {
        this.checkinService = checkinService;
    }

    private Integer userId(HttpServletRequest request) {
        return (Integer) request.getAttribute("userId");
    }

    @Operation(summary = "获取签到记录")
    @GetMapping
    public Map<String, Integer> getCheckins(HttpServletRequest request) {
        return checkinService.getHeatmap(userId(request));
    }
}
