package com.itheima.ai;

import com.itheima.domain.CardPattern;
import com.itheima.domain.Poker;
import com.itheima.game.Difficulty;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 单个 AI 玩家的独立对话上下文，负责调用模型、修复非法返回和压缩历史。
 */
public class AIPlayer {
    private final String name;
    private final Difficulty difficulty;
    private final DeepSeekClient client;
    private final Map<String, Object> systemMessage;
    private final List<Map<String, Object>> history = new ArrayList<>();
    private String historySummary;

    public AIPlayer(String name, Difficulty difficulty) {
        this.name = name;
        this.difficulty = difficulty;
        this.client = new DeepSeekClient();
        this.systemMessage = message("system",
                AIPromptBuilder.buildSystemPrompt(name, difficulty));
    }

    public AIAction decide(GameSnapshot snapshot, List<Poker> hand,
                           CardPattern previous) {
        String userMessage = AIPromptBuilder.buildUserMessage(snapshot);
        if (!difficulty.usesDeepSeek()) {
            return AIAction.fallback("简单难度使用本地策略").withUserMessage(userMessage);
        }
        if (!client.isConfigured()) {
            return AIAction.fallback("未配置 DEEPSEEK_API_KEY").withUserMessage(userMessage);
        }

        try {
            String raw = client.request(buildRequestMessages(userMessage));
            AIAction first = AIResponseParser.parse(raw, hand, previous, AIAction.Source.MODEL);
            if (first.isUsable()) {
                return first.withUserMessage(userMessage);
            }
            if (!first.isRetryable()) {
                return first.withUserMessage(userMessage);
            }

            List<Map<String, Object>> repairMessages = buildRequestMessages(userMessage);
            repairMessages.add(message("assistant", raw));
            repairMessages.add(message("user",
                    AIPromptBuilder.buildRepairMessage(first.getError(), snapshot.mustFollow)));
            String repaired = client.request(repairMessages);
            AIAction second = AIResponseParser.parse(repaired, hand, previous,
                    AIAction.Source.REPAIR);
            if (second.isUsable()) {
                return second.withUserMessage(userMessage);
            }
            if (!second.isRetryable()) {
                return second.withUserMessage(userMessage);
            }
            return AIAction.fallback(second.getError()).withUserMessage(userMessage);
        } catch (RuntimeException e) {
            return AIAction.fallback(e.getMessage()).withUserMessage(userMessage);
        }
    }

    public void recordDecision(AIAction action) {
        if (action.getUserMessage() == null) {
            return;
        }
        history.add(message("user", action.getUserMessage()));
        history.add(message("assistant", action.toNormalizedJson()));
        // 只保留最近 3 个 user/assistant 回合，长期信息由历史摘要承载。
        while (history.size() > 6) {
            history.remove(0);
            history.remove(0);
        }
    }

    public void setHistorySummary(String summary) {
        this.historySummary = summary;
    }

    private List<Map<String, Object>> buildRequestMessages(String currentUserMessage) {
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(systemMessage);
        if (historySummary != null && !historySummary.isBlank()) {
            messages.add(message("user", "【历史摘要】\n" + historySummary));
        }
        messages.addAll(history);
        messages.add(message("user", currentUserMessage));
        return messages;
    }

    private Map<String, Object> message(String role, String content) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    public String getName() {
        return name;
    }
}
