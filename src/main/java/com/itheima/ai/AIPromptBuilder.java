package com.itheima.ai;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.itheima.game.Difficulty;

/**
 * 负责生成固定 system prompt 和每轮紧凑 JSON user 消息。
 */
public final class AIPromptBuilder {
    private static final Gson GSON = new Gson();
    private static final String SYSTEM_TEMPLATE = """
            你是斗地主牌桌上的 {AI_PLAYER}。你与另一名 AI 玩家各自维护独立上下文，不能假设知道其他人的手牌或内部想法，只能依据自己的手牌、公开行动和局势信息决策。

            牌面顺序：3 < 4 < 5 < 6 < 7 < 8 < 9 < 10 < J < Q < K < A < 2 < 小王 < 大王。牌使用牌面字符串表示，重复点数在数组中以重复字符串出现。

            规则要点：
            1. must_follow=true 时必须用同一牌型且更大的牌压过 last_play；无法压过则 pass。
            2. 炸弹可以压过任何非炸弹牌型，点数更大的炸弹可以压过点数更小的炸弹；王炸最大。
            3. 顺子至少 5 张连续单牌；连对至少 3 组连续对子；两者都不能包含 2、小王或大王。
            4. 三带一为三张同点数牌加 1 张单牌；三带二为三张同点数牌加任意 2 张牌，不要求这 2 张组成对子。
            5. 飞机为至少 2 组连续三张，可以不带翅膀，也可以带与主体组数相同数量的单牌或对子；主体不能包含 2、小王或大王。
            6. 不支持四带二。
            7. must_follow=false 表示自由领出，此时不能 pass；连续两家 pass 后，由上一手出牌者重新自由领出。
            8. 先出完手牌的一方获胜；胜负由引擎判断。

            决策启发，以下不是硬规则：
            - 能管时权衡拿回出牌权与保留组合牌的价值，不要只为跟一手而拆散关键牌型。
            - 炸弹和王炸是控制资源，优先用于阻止对手走完、夺回关键出牌权或残局收官；倍数越高越谨慎。
            - 农民不要轻易压队友的有效牌；队友可能走完或明显占优时应主动让路，地主剩余牌少时优先阻止地主。
            - 地主应主动压制农民，特别是手牌少的农民，同时保留必要的控制牌和收官组合。
            - 残局优先计算能否直接出完或形成必胜控制。
            - 结合三家的手牌数、已出牌摘要、比分和倍数做动态判断。

            只输出一个 JSON 对象。不要输出 markdown、代码块、解释、前后缀或任何多余文字。格式：
            出牌：{"action":"play","cards":["3","4"]}
            不出：{"action":"pass"}
            cards 必须全部来自自己的手牌，并组成当前规则下的一种合法牌型。
            """;

    private AIPromptBuilder() {
    }

    public static String buildSystemPrompt(String aiPlayerName, Difficulty difficulty) {
        String prompt = SYSTEM_TEMPLATE.replace("{AI_PLAYER}", aiPlayerName);
        if (difficulty == Difficulty.HARD) {
            prompt += """

                    当前为困难难度。请在合法出牌前提下更精确地计算残局、牌权转换和对手剩余手牌，
                    优先选择能持续控制牌局或快速减少手牌的计划；不要为了短期跟牌破坏关键组合。
                    """;
        }
        return prompt;
    }

    public static String buildUserMessage(GameSnapshot snapshot) {
        JsonObject root = new JsonObject();
        root.addProperty("round", snapshot.roundNo);

        JsonObject me = new JsonObject();
        me.addProperty("player", snapshot.myPlayer);
        me.addProperty("role", snapshot.myRole);
        me.add("hand", GSON.toJsonTree(snapshot.myHand));
        root.add("me", me);

        root.addProperty("landlord", snapshot.landlord);
        root.add("farmers", GSON.toJsonTree(snapshot.farmers));
        root.add("counts", GSON.toJsonTree(snapshot.handCounts));
        root.add("recent_actions", GSON.toJsonTree(snapshot.recentActions));
        root.add("last_play", GSON.toJsonTree(snapshot.lastPlay));
        root.addProperty("must_follow", snapshot.mustFollow);
        root.addProperty("is_my_lead", snapshot.isMyLead);
        root.add("scores", GSON.toJsonTree(snapshot.scores));
        root.addProperty("multiplier", snapshot.multiplier);
        return GSON.toJson(root);
    }

    public static String buildRepairMessage(String errorReason, boolean mustFollow) {
        return "你上次的返回未通过引擎校验：" + errorReason
                + "。当前局面没有变化。请只输出一个合法 JSON 对象，必须使用自己的手牌，并满足 must_follow="
                + mustFollow + "。";
    }
}
