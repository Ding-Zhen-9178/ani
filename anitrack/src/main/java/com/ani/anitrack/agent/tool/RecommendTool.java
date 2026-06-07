package com.ani.anitrack.agent.tool;

import com.ani.anitrack.agent.UserContext;
import com.ani.anitrack.entity.Anime;
import com.ani.anitrack.service.AnimeService;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class RecommendTool {

    private final AnimeService animeService;

    public RecommendTool(AnimeService animeService) {
        this.animeService = animeService;
    }

    @Tool("根据追番习惯提供个性化「猜你喜欢」推荐")
    public String guessYouLike() {
        Integer userId = UserContext.get();
        if (userId == null) return "用户身份获取失败，请刷新页面重新登录";
        try {
            List<Anime> all = animeService.listAllOrderByUpdateDay(userId);

            if (all.isEmpty()) {
                return "你还没有添加任何番剧，先去添加几部吧！可以用 Bangumi 搜索找到你喜欢的番剧。";
            }

        StringBuilder sb = new StringBuilder();
        sb.append("🎯 猜你喜欢\n\n");

        // 规则1: 同日高分推荐 — 找出用户评分 >=7 的番剧的更新日，推荐同天未追的番剧
        List<Anime> highRated = all.stream()
                .filter(a -> a.getRating() != null && a.getRating() >= 7)
                .toList();

        if (!highRated.isEmpty()) {
            Set<Integer> favoriteDays = highRated.stream()
                    .map(Anime::getUpdateDay)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            List<Anime> sameDayRecs = all.stream()
                    .filter(a -> "watching".equals(a.getStatus())
                            && a.getUpdateDay() != null
                            && favoriteDays.contains(a.getUpdateDay())
                            && (a.getRating() == null || a.getRating() < 7))
                    .limit(3)
                    .toList();

            if (!sameDayRecs.isEmpty()) {
                sb.append("📺 基于你高分番剧的更新日，这些同天更新的番剧可能也适合你：\n");
                for (Anime a : sameDayRecs) {
                    sb.append(String.format("  %s | 周%d更新 | 评分:%s\n",
                            a.getName(), a.getUpdateDay(),
                            a.getRating() == null ? "未评" : a.getRating() + "分"));
                }
                sb.append("\n");
            }
        }

        // 规则2: 冷门提醒 — 追番中但超过14天未观看
        List<Anime> stale = all.stream()
                .filter(a -> "watching".equals(a.getStatus())
                        && a.getLastWatchDate() != null
                        && a.getLastWatchDate().compareTo(
                                java.time.LocalDate.now().minusDays(14).toString()) < 0)
                .toList();

        if (!stale.isEmpty()) {
            sb.append("⏰ 这些番剧超过两周没追了，别忘了它们：\n");
            for (Anime a : stale) {
                sb.append(String.format("  %s | 上次观看: %s | 进度:%d/%d\n",
                        a.getName(), a.getLastWatchDate(),
                        a.getCurrentEp(), a.getTotalEp() == null ? 0 : a.getTotalEp()));
            }
            sb.append("\n");
        }

        // 规则3: 追番节奏建议 — 按更新日分组统计
        Map<Integer, Long> dayCount = all.stream()
                .filter(a -> "watching".equals(a.getStatus()) && a.getUpdateDay() != null)
                .collect(Collectors.groupingBy(Anime::getUpdateDay, Collectors.counting()));

        if (!dayCount.isEmpty()) {
            Map.Entry<Integer, Long> busiest = Collections.max(dayCount.entrySet(), Map.Entry.comparingByValue());
            Map.Entry<Integer, Long> lightest = Collections.min(dayCount.entrySet(), Map.Entry.comparingByValue());

            sb.append("📊 追番节奏分析：\n");
            sb.append(String.format("  最忙更新日：周%d（%d 部）\n", busiest.getKey(), busiest.getValue()));
            sb.append(String.format("  最闲更新日：周%d（%d 部）\n", lightest.getKey(), lightest.getValue()));

            List<Integer> emptyDays = new ArrayList<>();
            for (int d = 1; d <= 7; d++) {
                if (!dayCount.containsKey(d)) {
                    emptyDays.add(d);
                }
            }
            if (!emptyDays.isEmpty()) {
                String days = emptyDays.stream().map(d -> "周" + d).collect(Collectors.joining("、"));
                sb.append(String.format("  %s 暂无番剧，可以考虑添加新番\n", days));
            }
            sb.append("\n");
        }

        // 规则4: 补番推荐 — 想看但还没开始看的
        List<Anime> backlog = all.stream()
                .filter(a -> "want_to_watch".equals(a.getStatus()) && a.getRating() != null && a.getRating() >= 7)
                .limit(3)
                .toList();

        if (!backlog.isEmpty()) {
            sb.append("🍿 你的「想看」列表里有这些高分番剧，不妨开始补：\n");
            for (Anime a : backlog) {
                sb.append(String.format("  %s | 评分:%d | 共%d集\n",
                        a.getName(), a.getRating(),
                        a.getTotalEp() == null ? 0 : a.getTotalEp()));
            }
        }

            return sb.toString();
        } catch (Exception e) {
            return "生成推荐失败：" + e.getMessage();
        }
    }
}
