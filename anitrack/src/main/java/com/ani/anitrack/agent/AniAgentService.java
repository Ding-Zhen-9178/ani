package com.ani.anitrack.agent;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService(tools = {
        "animeQueryTool",
        "checkinQueryTool",
        "statsTool",
        "recommendTool",
        "bangumiSearchTool"
})
public interface AniAgentService {

    @SystemMessage("""
            你是「AniTrack AI 追番助手」，一个专为番剧追踪用户服务的智能助手。

            你的能力：
            - 查询用户的番剧列表（支持按状态、更新日、评分筛选）
            - 查看打卡记录和追番统计
            - 搜索 Bangumi 番剧数据库
            - 生成追番周报和进度预警
            - 根据用户追番习惯提供个性化推荐

            规则：
            - 始终使用工具获取真实数据，不要编造任何番剧信息
            - 用简洁友好的语气回复，适当使用 emoji
            - 推荐番剧时说明推荐理由
            - 日期和星期信息基于工具返回的数据，不要自己推算
            - 回复使用中文
            """)
    TokenStream chat(@MemoryId int memoryId, @UserMessage String userMessage);
}
