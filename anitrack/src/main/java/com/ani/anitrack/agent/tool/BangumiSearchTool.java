package com.ani.anitrack.agent.tool;

import com.ani.anitrack.service.BangumiService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class BangumiSearchTool {

    private final BangumiService bangumiService;

    public BangumiSearchTool(BangumiService bangumiService) {
        this.bangumiService = bangumiService;
    }

    @Tool("在 Bangumi 番剧数据库中搜索番剧信息")
    public String searchBangumi(@P("搜索关键词，可以是番剧名称") String keyword) {
        try {
            List<Map<String, Object>> results = bangumiService.search(keyword);

            if (results.isEmpty()) {
                return "未找到与「" + keyword + "」相关的番剧";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("搜索「").append(keyword).append("」的结果：\n");

            int count = 0;
            for (Map<String, Object> item : results) {
                if (count >= 5) break;
                String name = String.valueOf(item.getOrDefault("name", "未知"));
                String nameCn = item.get("name_cn") != null ? String.valueOf(item.get("name_cn")) : "";
                String displayName = nameCn.isEmpty() ? name : nameCn + " / " + name;

                Object ratingObj = item.get("rating");
                String rating = "暂无评分";
                if (ratingObj instanceof Map<?, ?> ratingMap && ratingMap.get("score") != null) {
                    rating = ratingMap.get("score") + "分";
                }

                sb.append(String.format("%d. %s | 评分: %s\n",
                        count + 1, displayName, rating));
                count++;
            }

            return sb.toString();
        } catch (Exception e) {
            return "搜索服务暂不可用: " + e.getMessage();
        }
    }
}
