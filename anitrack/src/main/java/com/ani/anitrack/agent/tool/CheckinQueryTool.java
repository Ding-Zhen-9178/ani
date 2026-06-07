package com.ani.anitrack.agent.tool;

import com.ani.anitrack.agent.UserContext;
import com.ani.anitrack.service.CheckinService;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;

@Component
public class CheckinQueryTool {

    private final CheckinService checkinService;

    public CheckinQueryTool(CheckinService checkinService) {
        this.checkinService = checkinService;
    }

    private Integer getUserId() {
        Integer userId = UserContext.get();
        if (userId == null) return null;
        return userId;
    }

    @Tool("获取打卡热力图数据，返回每天的追番集数")
    public String getHeatmap() {
        Integer userId = getUserId();
        if (userId == null) return "用户身份获取失败，请刷新页面重新登录";
        try {
            Map<String, Integer> heatmap = checkinService.getHeatmap(userId);

            if (heatmap.isEmpty()) {
                return "暂无打卡记录";
            }

            int totalDays = heatmap.size();
            int totalCount = heatmap.values().stream().mapToInt(Integer::intValue).sum();

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("打卡总览：%d 天打卡，共追 %d 集\n\n", totalDays, totalCount));

            sb.append("最近7天打卡记录：\n");
            LocalDate today = LocalDate.now();
            for (int i = 6; i >= 0; i--) {
                String date = today.minusDays(i).toString();
                Integer count = heatmap.getOrDefault(date, 0);
                String dayOfWeek = dayLabel(today.minusDays(i).getDayOfWeek());
                sb.append(String.format("%s (%s): %d 集\n", date, dayOfWeek, count));
            }

            return sb.toString();
        } catch (Exception e) {
            return "获取打卡数据失败：" + e.getMessage();
        }
    }

    @Tool("获取本周每天打卡统计")
    public String getWeeklyStats() {
        Integer userId = getUserId();
        if (userId == null) return "用户身份获取失败，请刷新页面重新登录";
        try {
            Map<String, Integer> heatmap = checkinService.getHeatmap(userId);

            LocalDate today = LocalDate.now();
            LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

            StringBuilder sb = new StringBuilder();
            sb.append("本周打卡统计：\n");

            int weekTotal = 0;
            int weekDays = 0;
            for (int i = 0; i < 7; i++) {
                String date = weekStart.plusDays(i).toString();
                Integer count = heatmap.getOrDefault(date, 0);
                String dayOfWeek = dayLabel(weekStart.plusDays(i).getDayOfWeek());
                sb.append(String.format("  %s %s: %d 集\n", date, dayOfWeek, count));
                if (count > 0) {
                    weekTotal += count;
                    weekDays++;
                }
            }

            sb.append(String.format("\n本周合计：%d 天打卡，共 %d 集", weekDays, weekTotal));
            return sb.toString();
        } catch (Exception e) {
            return "获取周统计失败：" + e.getMessage();
        }
    }

    @Tool("获取累计追番总集数")
    public String getTotalWatchCount() {
        Integer userId = getUserId();
        if (userId == null) return "用户身份获取失败，请刷新页面重新登录";
        try {
            Map<String, Integer> heatmap = checkinService.getHeatmap(userId);
            int total = heatmap.values().stream().mapToInt(Integer::intValue).sum();
            return String.format("累计追番 %d 集，分布在 %d 天", total, heatmap.size());
        } catch (Exception e) {
            return "获取累计追番数据失败：" + e.getMessage();
        }
    }

    private String dayLabel(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "周一";
            case TUESDAY -> "周二";
            case WEDNESDAY -> "周三";
            case THURSDAY -> "周四";
            case FRIDAY -> "周五";
            case SATURDAY -> "周六";
            case SUNDAY -> "周日";
        };
    }
}
