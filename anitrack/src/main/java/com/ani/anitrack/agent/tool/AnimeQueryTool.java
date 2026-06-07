package com.ani.anitrack.agent.tool;

import com.ani.anitrack.agent.UserContext;
import com.ani.anitrack.entity.Anime;
import com.ani.anitrack.service.AnimeService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AnimeQueryTool {

    private final AnimeService animeService;

    public AnimeQueryTool(AnimeService animeService) {
        this.animeService = animeService;
    }

    @Tool("列出我的番剧列表，可按状态和更新日筛选")
    public String listMyAnime(
            @P("状态筛选，可选: watching/completed/dropped/want_to_watch，不传返回全部")
            String status,
            @P("更新日筛选，1=周一...7=周日，不传返回全部")
            Integer day) {
        Integer userId = UserContext.get();
        if (userId == null) return "用户身份获取失败，请刷新页面重新登录";
        try {
            List<Anime> list = animeService.listAllOrderByUpdateDay(userId);

            if (status != null && !status.isBlank()) {
                list = list.stream().filter(a -> status.equals(a.getStatus())).toList();
            }
            if (day != null) {
                list = list.stream().filter(a -> day.equals(a.getUpdateDay())).toList();
            }

            if (list.isEmpty()) {
                return "暂无番剧记录";
            }

            return list.stream()
                    .map(a -> String.format(
                            "[ID:%d] %s | 进度:%d/%d | 更新日:周%d | 评分:%s | 状态:%s",
                            a.getId(), a.getName(),
                            a.getCurrentEp(), a.getTotalEp() == null ? 0 : a.getTotalEp(),
                            a.getUpdateDay(),
                            a.getRating() == null ? "未评" : a.getRating() + "分",
                            statusLabel(a.getStatus())))
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "查询番剧列表失败：" + e.getMessage();
        }
    }

    @Tool("获取单个番剧的详细信息")
    public String getAnimeDetail(@P("番剧ID") Integer animeId) {
        Integer userId = UserContext.get();
        if (userId == null) return "用户身份获取失败，请刷新页面重新登录";
        try {
            Anime a = animeService.getAnime(animeId, userId);
            if (a == null) {
                return "未找到该番剧";
            }
            return String.format("""
                            番名: %s
                            ID: %d
                            进度: %d/%d
                            更新日: 周%d
                            评分: %s
                            状态: %s
                            上次观看: %s
                            网址: %s""",
                    a.getName(), a.getId(),
                    a.getCurrentEp(), a.getTotalEp() == null ? 0 : a.getTotalEp(),
                    a.getUpdateDay(),
                    a.getRating() == null ? "未评" : a.getRating() + "分",
                    statusLabel(a.getStatus()),
                    a.getLastWatchDate() == null ? "暂无" : a.getLastWatchDate(),
                    a.getWebsiteUrl() == null ? "无" : a.getWebsiteUrl());
        } catch (Exception e) {
            return "查询番剧详情失败：" + e.getMessage();
        }
    }

    @Tool("在用户的番剧列表中按名称搜索")
    public String searchMyAnime(@P("搜索关键词") String keyword) {
        Integer userId = UserContext.get();
        if (userId == null) return "用户身份获取失败，请刷新页面重新登录";
        try {
            List<Anime> list = animeService.listAllOrderByUpdateDay(userId);
            String kw = keyword.toLowerCase();
            List<Anime> matched = list.stream()
                    .filter(a -> a.getName() != null && a.getName().toLowerCase().contains(kw))
                    .toList();

            if (matched.isEmpty()) {
                return "未找到包含「" + keyword + "」的番剧";
            }

            return matched.stream()
                    .map(a -> String.format("[ID:%d] %s | %d/%d | 周%d | %s",
                            a.getId(), a.getName(), a.getCurrentEp(),
                            a.getTotalEp() == null ? 0 : a.getTotalEp(),
                            a.getUpdateDay(), statusLabel(a.getStatus())))
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "搜索番剧失败：" + e.getMessage();
        }
    }

    @Tool("获取今天更新的番剧列表")
    public String getTodayAnime() {
        Integer userId = UserContext.get();
        if (userId == null) return "用户身份获取失败，请刷新页面重新登录";
        try {
            List<Anime> list = animeService.listAllOrderByUpdateDay(userId);
            int today = java.time.LocalDate.now().getDayOfWeek().getValue(); // 1=Mon..7=Sun
            List<Anime> todayList = list.stream()
                    .filter(a -> a.getUpdateDay() != null && a.getUpdateDay() == today)
                    .toList();

            if (todayList.isEmpty()) {
                return "今天（周" + today + "）没有需要更新的番剧";
            }

            return "今天（周" + today + "）更新的番剧：\n" + todayList.stream()
                    .map(a -> String.format("[ID:%d] %s | 进度:%d/%d | 评分:%s",
                            a.getId(), a.getName(),
                            a.getCurrentEp(), a.getTotalEp() == null ? 0 : a.getTotalEp(),
                            a.getRating() == null ? "未评" : a.getRating() + "分"))
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "查询今日更新失败：" + e.getMessage();
        }
    }

    @Tool("按评分筛选番剧")
    public String getAnimeByRating(@P("最低评分，1-10") Integer minRating) {
        Integer userId = UserContext.get();
        if (userId == null) return "用户身份获取失败，请刷新页面重新登录";
        try {
            List<Anime> list = animeService.listAllOrderByUpdateDay(userId);
            List<Anime> highRated = list.stream()
                    .filter(a -> a.getRating() != null && a.getRating() >= minRating)
                    .toList();

            if (highRated.isEmpty()) {
                return "没有评分 >= " + minRating + " 的番剧";
            }

            return highRated.stream()
                    .map(a -> String.format("[ID:%d] %s | 评分:%d | 进度:%d/%d | 周%d",
                            a.getId(), a.getName(), a.getRating(),
                            a.getCurrentEp(), a.getTotalEp() == null ? 0 : a.getTotalEp(),
                            a.getUpdateDay()))
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "按评分筛选失败：" + e.getMessage();
        }
    }

    private String statusLabel(String status) {
        return switch (status) {
            case "watching" -> "追番中";
            case "completed" -> "已完结";
            case "dropped" -> "已弃坑";
            case "want_to_watch" -> "想看";
            default -> status;
        };
    }
}
