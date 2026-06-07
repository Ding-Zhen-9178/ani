package com.ani.anitrack.controller;

import com.ani.anitrack.agent.AniAgentService;
import com.ani.anitrack.agent.UserContext;
import dev.langchain4j.service.TokenStream;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "AI 助手", description = "基于 LangChain4j + DeepSeek 的智能追番助手")
public class ChatController {

    private final AniAgentService agentService;

    public ChatController(AniAgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping
    @Operation(summary = "与 AI 助手对话（流式输出）")
    public SseEmitter chat(@RequestBody Map<String, String> body,
                           HttpServletRequest request) {
        String message = body.getOrDefault("message", "").trim();
        SseEmitter emitter = new SseEmitter(120_000L);

        if (message.isEmpty()) {
            try {
                emitter.send(SseEmitter.event().data("请发送一条消息"));
            } catch (IOException ignored) {}
            emitter.complete();
            return emitter;
        }

        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            try {
                emitter.send(SseEmitter.event().data("用户身份验证失败，请重新登录"));
            } catch (IOException ignored) {}
            emitter.complete();
            return emitter;
        }

        TokenStream tokenStream = agentService.chat(userId, message);

        UserContext.set(userId);
        tokenStream
                .onPartialResponse(token -> {
                    try {
                        emitter.send(SseEmitter.event().data(token));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                })
                .onCompleteResponse(response -> {
                    UserContext.clear();
                    emitter.complete();
                })
                .onError(error -> {
                    UserContext.clear();
                    emitter.completeWithError(error);
                })
                .start();

        return emitter;
    }
}
