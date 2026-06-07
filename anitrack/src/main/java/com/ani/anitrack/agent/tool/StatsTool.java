package com.ani.anitrack.agent.tool;

import com.ani.anitrack.agent.UserContext;
import com.ani.anitrack.entity.Anime;
import com.ani.anitrack.service.AnimeService;
import com.ani.anitrack.service.CheckinService;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class StatsTool {

    private final AnimeService animeService;
    private final CheckinService checkinService;

    public StatsTool(AnimeService animeService, CheckinService checkinService) {
        this.animeService = animeService;
        this.checkinService = checkinService;
    }

    @Tool("获取番剧库整体概览数据：各状态的番剧数量统计")
    public String getDashboard() {
        Integer userId = UserContext.get();
        if (userId == null) return "用户身份获取失败，请刷新页面重新登录";
        try {
            List<Anime> list = animeService.listAllOrderByUpdateDay(userId);

            long watching = list.stream().filter(a -> "watching".equals(a.getStatus())).count();
            long completed = list.stream().filter(a -> "completed".equals(a.getStatus())).count();
            long dropped = list.stream().filter(a -> "dropped".equals(a.getStatus())).count();
            long wantToWatch = list.stream().filter(a -> "want_to_watch".equals(a.getStatus())).count();

            double avgRating = list.stream()
                    .filter(a -> a.getRating() != null)
                    .mapToInt(Anime::getRating)
                    .average()
                    .orElse(0);

            return String.format("""
                            番剧库概览：
                            总计：%d 部
                            追番中：%d 部
                            已完结：%d 部
                            想看：%d 部
                            已弃坑：%d 部
                            平均评分：%.1f 分""",
                    list.size(), watching, completed, wantToWatch, dropped, avgRating);
        } catch (Exception e) {
            return "获取番剧概览失败：" + e.getMessage();
        }
    }

    @Tool("生成追番周报：本周打卡情况、进度变化、追番建议")
    public String getWeeklyReport() {
        Integer userId = UserContext.get();
        if (userId == null) return "用户身份获取失败，请刷新页面重新登录";
        try {
            List<Anime> list = animeService.listAllOrderByUpdateDay(userId);
            Map<String, Integer> heatmap = checkinService.getHeatmap(userId);

            LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);

        // 本周打卡
        int weekDays = 0;
        int weekEpisodes = 0;
        for (int i = 0; i < 7; i++) {
            String date = weekStart.plusDays(i).toString();
            Integer count = heatmap.getOrDefault(date, 0);
            if (count > 0) {
                weekDays++;
                weekEpisodes += count;
            }
        }

        // 追番中且本周有更新的番剧
        int todayDayOfWeek = today.getDayOfWeek().getValue();
        List<Anime> watchingToday = list.stream()
                .filter(a -> "watching".equals(a.getStatus()) && a.getUpdateDay() != null && a.getUpdateDay() == todayDayOfWeek)
                .toList();

        // 进度落后检测：追番中但 lastWatchDate 超过 7 天
        LocalDate sevenDaysAgo = today.minusDays(7);
        List<Anime> stale = list.stream()
                .filter(a -> "watching".equals(a.getStatus())
                        && a.getLastWatchDate() != null
                        && a.getLastWatchDate().compareTo(sevenDaysAgo.toString()) < 0)
                .toList();

        StringBuilder sb = new StringBuilder();
        sb.append("📊 追番周报（").append(weekStart).append(" ~ ").append(today).append("）\n\n");

        sb.append("本周数据：\n");
        sb.append(String.format("  打卡 %d 天，追了 %d 集\n", weekDays, weekEpisodes));

        if (!watchingToday.isEmpty()) {
            sb.append(String.format("\n📅 今天（周%d）更新的番剧：\n", todayDayOfWeek));
            for (Anime a : watchingToday) {
                sb.append(String.format("  %s | 进度:%d/%d\n",
                        a.getName(), a.getCurrentEp(),
                        a.getTotalEp() == null ? 0 : a.getTotalEp()));
            }
        }

        if (!stale.isEmpty()) {
            sb.append("\n⚠️ 超过一周未观看的番剧：\n");
            for (Anime a : stale) {
                sb.append(String.format("  %s | 上次观看: %s | 进度:%d/%d\n",
                        a.getName(), a.getLastWatchDate(),
                        a.getCurrentEp(), a.getTotalEp() == null ? 0 : a.getTotalEp()));
            }
        }

            if (weekDays == 0) {
                sb.append("\n💪 本周还没有打卡记录，今天开始追番吧！");
            }

            return sb.toString();
        } catch (Exception e) {
            return "生成周报失败：" + e.getMessage();
        }
    }

    @Tool("获取进度预警：哪些番剧需要追、哪些进度落后")
    public String getProgressAlerts() {
        Integer userId = UserContext.get();
        if (userId == null) return "用户身份获取失败，请刷新页面重新登录";
        try {
        List<Anime> list = animeService.listAllOrderByUpdateDay(userId);
        LocalDate today = LocalDate.now();
        int todayDayOfWeek = today.getDayOfWeek().getValue();

        StringBuilder sb = new StringBuilder();
        sb.append("🔔 进度预警\n\n");

        // 今天更新的番剧
        List<Anime> todayAnime = list.stream()
                .filter(a -> "watching".equals(a.getStatus()) && a.getUpdateDay() != null && a.getUpdateDay() == todayDayOfWeek)
                .toList();

        if (!todayAnime.isEmpty()) {
            sb.append("📅 今天待追：\n");
            for (Anime a : todayAnime) {
                sb.append(String.format("  %s | 当前第%d集\n", a.getName(), a.getCurrentEp()));
            }
        }

        // 积压超过14天的
        LocalDate fourteenDaysAgo = today.minusDays(14);
        List<Anime> staleAnime = list.stream()
                .filter(a -> "watching".equals(a.getStatus())
                        && a.getLastWatchDate() != null
                        && a.getLastWatchDate().compareTo(fourteenDaysAgo.toString()) < 0)
                .toList();

        if (!staleAnime.isEmpty()) {
            sb.append("\n⚠️ 超过两周未观看：\n");
            for (Anime a : staleAnime) {
                sb.append(String.format("  %s | 上次: %s | 进度:%d/%d\n",
                        a.getName(), a.getLastWatchDate(),
                        a.getCurrentEp(), a.getTotalEp() == null ? 0 : a.getTotalEp()));
            }
        }

        // 进度落后：追番中但停在很早期
        List<Anime> lagging = list.stream()
                .filter(a -> "watching".equals(a.getStatus())
                        && a.getCurrentEp() != null && a.getCurrentEp() <= 3
                        && a.getTotalEp() != null && a.getTotalEp() > 10)
                .toList();

        if (!lagging.isEmpty()) {
            sb.append("\n🐌 进度落后的番剧（刚开头）：\n");
            for (Anime a : lagging) {
                sb.append(String.format("  %s | 第%d集 / 共%d集\n",
                        a.getName(), a.getCurrentEp(), a.getTotalEp()));
            }
        }

            if (todayAnime.isEmpty() && staleAnime.isEmpty() && lagging.isEmpty()) {
                sb.append("暂无预警，追番进度良好！");
            }

            return sb.toString();
        } catch (Exception e) {
            return "获取进度预警失败：" + e.getMessage();
        }
    }
}
