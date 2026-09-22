package com.itheima.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.itheima.domain.CardPattern;
import com.itheima.domain.Poker;
import com.itheima.rules.CardRules;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 严格解析模型返回，并把牌面字符串映射为玩家手牌中的真实 Poker 对象。
 */
public final class AIResponseParser {
    private AIResponseParser() {
    }

    public static AIAction parse(String raw, List<Poker> hand,
                                 CardPattern previous, AIAction.Source source) {
        JsonObject object;
        try {
            object = parseStrictObject(raw);
        } catch (RuntimeException | IOException e) {
            return AIAction.invalid("返回不是严格的单个 JSON 对象", true);
        }

        JsonElement actionElement = object.get("action");
        if (actionElement == null || !actionElement.isJsonPrimitive()
                || !actionElement.getAsJsonPrimitive().isString()) {
            return AIAction.invalid("缺少合法的 action 字段", true);
        }
        String action = actionElement.getAsString();
        if ("pass".equals(action)) {
            if (previous == null) {
                return AIAction.invalid("自由领出时不允许 pass", false);
            }
            if (object.has("cards")) {
                return AIAction.invalid("pass 不能携带 cards 字段", true);
            }
            return AIAction.pass(source);
        }
        if (!"play".equals(action)) {
            return AIAction.invalid("action 只能是 play 或 pass", true);
        }

        JsonElement cardsElement = object.get("cards");
        if (cardsElement == null || !cardsElement.isJsonArray()) {
            return AIAction.invalid("play 缺少 cards 数组", true);
        }
        JsonArray cardsArray = cardsElement.getAsJsonArray();
        if (cardsArray.isEmpty()) {
            return AIAction.invalid("cards 不能为空", true);
        }

        List<String> ranks = new ArrayList<>(cardsArray.size());
        for (JsonElement cardElement : cardsArray) {
            if (!cardElement.isJsonPrimitive()
                    || !((JsonPrimitive) cardElement).isString()) {
                return AIAction.invalid("cards 必须全部是字符串", true);
            }
            ranks.add(cardElement.getAsString());
        }

        List<Poker> selectedCards = selectCards(hand, ranks);
        if (selectedCards == null) {
            return AIAction.invalid("使用了不在手牌中的牌", true);
        }
        CardPattern pattern = CardRules.parse(selectedCards);
        if (pattern == null) {
            return AIAction.invalid("所选牌无法组成合法牌型", true);
        }
        if (previous != null && !CardRules.canBeat(pattern, previous)) {
            return AIAction.invalid("所出牌无法压过上一手", false);
        }
        return AIAction.play(selectedCards, pattern, source);
    }

    private static JsonObject parseStrictObject(String raw) throws IOException {
        if (raw == null || raw.isBlank()) {
            throw new IOException("empty response");
        }
        try (JsonReader reader = new JsonReader(new StringReader(raw))) {
            reader.setLenient(false);
            JsonElement element = JsonParser.parseReader(reader);
            if (reader.peek() != JsonToken.END_DOCUMENT) {
                throw new IOException("trailing content");
            }
            if (!element.isJsonObject()) {
                throw new IOException("not an object");
            }
            return element.getAsJsonObject();
        }
    }

    private static List<Poker> selectCards(List<Poker> hand, List<String> ranks) {
        Map<String, List<Poker>> cardsByRank = new HashMap<>();
        for (Poker poker : hand) {
            cardsByRank.computeIfAbsent(poker.getRank(), ignored -> new ArrayList<>())
                    .add(poker);
        }
        Map<String, Integer> used = new HashMap<>();
        List<Poker> selected = new ArrayList<>();
        for (String rank : ranks) {
            int index = used.getOrDefault(rank, 0);
            List<Poker> candidates = cardsByRank.get(rank);
            if (candidates == null || index >= candidates.size()) {
                return null;
            }
            selected.add(candidates.get(index));
            used.put(rank, index + 1);
        }
        return selected;
    }
}
