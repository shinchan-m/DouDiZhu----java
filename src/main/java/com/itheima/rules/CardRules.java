package com.itheima.rules;

import com.itheima.domain.CardPattern;
import com.itheima.domain.Poker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * 斗地主牌型引擎，负责识别牌型、比较大小和生成 AI 本地兜底出牌。
 */
public final class CardRules {
    private CardRules() {
    }

    /**
     * 解析一次出牌的牌型，无法识别时返回 null。
     */
    public static CardPattern parse(Collection<Poker> cards) {
        if (cards == null || cards.isEmpty()) {
            return null;
        }
        List<Poker> list = new ArrayList<>(cards);
        Map<Integer, Integer> counts = countRanks(list);
        int size = list.size();
        List<Integer> ranks = new ArrayList<>(counts.keySet());

        if (size == 2 && counts.containsKey(16) && counts.containsKey(17)) {
            return pattern(CardPattern.Type.ROCKET, 17, size, List.of(17));
        }

        if (counts.size() == 1) {
            int value = ranks.get(0);
            if (size == 1) {
                return pattern(CardPattern.Type.SINGLE, value, size, List.of(value));
            }
            if (size == 2) {
                return pattern(CardPattern.Type.PAIR, value, size, List.of(value));
            }
            if (size == 3) {
                return pattern(CardPattern.Type.TRIPLE, value, size, List.of(value));
            }
            if (size == 4) {
                return pattern(CardPattern.Type.BOMB, value, size, List.of(value));
            }
        }

        if (size == 4) {
            for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
                if (entry.getValue() == 3) {
                    return pattern(CardPattern.Type.TRIPLE_ONE, entry.getKey(), size,
                            List.of(entry.getKey()));
                }
            }
        }

        if (size == 5) {
            for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
                if (entry.getValue() == 3) {
                    return pattern(CardPattern.Type.TRIPLE_TWO, entry.getKey(), size,
                            List.of(entry.getKey()));
                }
            }
        }

        if (size >= 5 && allCountsEqual(counts, 1) && isConsecutive(ranks, 14)) {
            return pattern(CardPattern.Type.STRAIGHT, ranks.get(ranks.size() - 1), size, ranks);
        }

        if (size >= 6 && size % 2 == 0 && allCountsEqual(counts, 2)
                && isConsecutive(ranks, 14)) {
            return pattern(CardPattern.Type.PAIR_STRAIGHT, ranks.get(ranks.size() - 1), size, ranks);
        }

        // 飞机带有多种翅膀组合，单独按连续三张主体解析。
        CardPattern plane = parsePlane(counts, size);
        if (plane != null) {
            return plane;
        }
        return null;
    }

    private static CardPattern parsePlane(Map<Integer, Integer> counts, int size) {
        List<Integer> bodyCandidates = counts.entrySet().stream()
                .filter(entry -> entry.getValue() == 3 && entry.getKey() <= 14)
                .map(Map.Entry::getKey)
                .sorted()
                .toList();

        for (int length = bodyCandidates.size(); length >= 2; length--) {
            for (int start = 0; start + length <= bodyCandidates.size(); start++) {
                List<Integer> body = bodyCandidates.subList(start, start + length);
                if (!isConsecutive(body, 14)) {
                    continue;
                }
                if (size == 3 * length) {
                    return pattern(CardPattern.Type.PLANE, body.get(length - 1), size, body);
                }
                if (size == 4 * length && hasSingleWings(counts, body, length)) {
                    return pattern(CardPattern.Type.PLANE_SINGLE,
                            body.get(length - 1), size, body);
                }
                if (size == 5 * length && hasPairWings(counts, body, length)) {
                    return pattern(CardPattern.Type.PLANE_PAIR,
                            body.get(length - 1), size, body);
                }
            }
        }
        return null;
    }

    private static boolean hasSingleWings(Map<Integer, Integer> counts,
                                          List<Integer> body, int wingCount) {
        int wings = 0;
        for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
            if (!body.contains(entry.getKey())) {
                wings += entry.getValue();
            }
        }
        return wings == wingCount;
    }

    private static boolean hasPairWings(Map<Integer, Integer> counts,
                                        List<Integer> body, int pairCount) {
        int pairs = 0;
        for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
            if (body.contains(entry.getKey())) {
                continue;
            }
            if (entry.getValue() != 2) {
                return false;
            }
            pairs++;
        }
        return pairs == pairCount;
    }

    /**
     * 判断候选牌是否能够压过上一手有效牌。
     */
    public static boolean canBeat(CardPattern candidate, CardPattern previous) {
        if (candidate == null) {
            return false;
        }
        if (previous == null) {
            return true;
        }
        if (candidate.type() == CardPattern.Type.ROCKET) {
            return true;
        }
        if (previous.type() == CardPattern.Type.ROCKET) {
            return false;
        }
        if (candidate.type() == CardPattern.Type.BOMB
                && previous.type() != CardPattern.Type.BOMB) {
            return true;
        }
        if (candidate.type() != previous.type()) {
            return false;
        }
        if (candidate.cardCount() != previous.cardCount()) {
            return false;
        }
        return candidate.mainValue() > previous.mainValue();
    }

    /**
     * 生成当前手牌中的全部合法组合，供本地兜底策略使用。
     */
    public static List<List<Poker>> findAllLegalPlays(Collection<Poker> hand,
                                                      CardPattern previous) {
        Map<Integer, List<Poker>> cardsByValue = groupCards(hand);
        Map<String, List<Poker>> unique = new LinkedHashMap<>();
        List<Integer> values = new ArrayList<>(cardsByValue.keySet());
        values.sort(Integer::compareTo);

        addCountCombinations(unique, cardsByValue, values, 1);
        addCountCombinations(unique, cardsByValue, values, 2);
        addCountCombinations(unique, cardsByValue, values, 3);
        addCountCombinations(unique, cardsByValue, values, 4);
        addRocket(unique, cardsByValue);
        addTripleWithAttachments(unique, cardsByValue, values);
        addSequences(unique, cardsByValue, false);
        addSequences(unique, cardsByValue, true);
        addPlanes(unique, cardsByValue, values);

        List<List<Poker>> result = new ArrayList<>();
        for (List<Poker> candidateCards : unique.values()) {
            CardPattern pattern = parse(candidateCards);
            if (pattern != null && canBeat(pattern, previous)) {
                result.add(candidateCards);
            }
        }
        result.sort(Comparator
                .comparingInt((List<Poker> cards) -> parse(cards).isBomb() ? 1 : 0)
                .thenComparingInt(List::size)
                .thenComparingInt(cards -> parse(cards).mainValue()));
        return result;
    }

    private static void addCountCombinations(Map<String, List<Poker>> unique,
                                             Map<Integer, List<Poker>> cardsByValue,
                                             List<Integer> values, int count) {
        for (int value : values) {
            List<Poker> cards = cardsByValue.get(value);
            if (cards.size() >= count) {
                addUnique(unique, new ArrayList<>(cards.subList(0, count)));
            }
        }
    }

    private static void addRocket(Map<String, List<Poker>> unique,
                                  Map<Integer, List<Poker>> cardsByValue) {
        if (cardsByValue.containsKey(16) && cardsByValue.containsKey(17)) {
            List<Poker> rocket = new ArrayList<>();
            rocket.add(cardsByValue.get(16).get(0));
            rocket.add(cardsByValue.get(17).get(0));
            addUnique(unique, rocket);
        }
    }

    private static void addTripleWithAttachments(Map<String, List<Poker>> unique,
                                                 Map<Integer, List<Poker>> cardsByValue,
                                                 List<Integer> values) {
        for (int triple : values) {
            List<Poker> tripleCards = cardsByValue.get(triple);
            if (tripleCards.size() < 3) {
                continue;
            }
            List<Poker> attachments = new ArrayList<>();
            for (int value : values) {
                if (value != triple) {
                    attachments.addAll(cardsByValue.get(value));
                }
            }
            if (!attachments.isEmpty()) {
                List<Poker> tripleOne = new ArrayList<>(tripleCards.subList(0, 3));
                tripleOne.add(attachments.get(0));
                addUnique(unique, tripleOne);
            }
            if (attachments.size() >= 2) {
                List<Poker> tripleTwo = new ArrayList<>(tripleCards.subList(0, 3));
                tripleTwo.addAll(attachments.subList(0, 2));
                addUnique(unique, tripleTwo);
            }
        }
    }

    private static void addSequences(Map<String, List<Poker>> unique,
                                     Map<Integer, List<Poker>> cardsByValue,
                                     boolean pairs) {
        int required = pairs ? 2 : 1;
        int minLength = pairs ? 3 : 5;
        for (int start = 3; start <= 14; start++) {
            List<Poker> sequence = new ArrayList<>();
            for (int value = start; value <= 14; value++) {
                List<Poker> cards = cardsByValue.get(value);
                if (cards == null || cards.size() < required) {
                    break;
                }
                sequence.addAll(cards.subList(0, required));
                int length = value - start + 1;
                if (length >= minLength) {
                    addUnique(unique, new ArrayList<>(sequence));
                }
            }
        }
    }

    private static void addPlanes(Map<String, List<Poker>> unique,
                                  Map<Integer, List<Poker>> cardsByValue,
                                  List<Integer> values) {
        List<Integer> tripleValues = values.stream()
                .filter(value -> value <= 14 && cardsByValue.get(value).size() >= 3)
                .toList();
        for (int start = 0; start < tripleValues.size(); start++) {
            for (int end = start + 1; end < tripleValues.size(); end++) {
                List<Integer> body = tripleValues.subList(start, end + 1);
                if (!isConsecutive(body, 14)) {
                    break;
                }
                List<Poker> bodyCards = new ArrayList<>();
                for (int value : body) {
                    bodyCards.addAll(cardsByValue.get(value).subList(0, 3));
                }
                addUnique(unique, new ArrayList<>(bodyCards));

                List<Poker> singlePool = new ArrayList<>();
                List<List<Poker>> pairPool = new ArrayList<>();
                for (int value : values) {
                    if (!body.contains(value)) {
                        singlePool.addAll(cardsByValue.get(value));
                        if (cardsByValue.get(value).size() >= 2) {
                            pairPool.add(new ArrayList<>(cardsByValue.get(value).subList(0, 2)));
                        }
                    }
                }
                if (singlePool.size() >= body.size()) {
                    List<Poker> withSingles = new ArrayList<>(bodyCards);
                    withSingles.addAll(singlePool.subList(0, body.size()));
                    addUnique(unique, withSingles);
                }
                if (pairPool.size() >= body.size()) {
                    List<Poker> withPairs = new ArrayList<>(bodyCards);
                    for (int i = 0; i < body.size(); i++) {
                        withPairs.addAll(pairPool.get(i));
                    }
                    addUnique(unique, withPairs);
                }
            }
        }
    }

    private static void addUnique(Map<String, List<Poker>> unique, List<Poker> cards) {
        if (cards.isEmpty()) {
            return;
        }
        List<Integer> values = cards.stream().map(Poker::getValue).sorted().toList();
        unique.putIfAbsent(values.toString(), cards);
    }

    private static Map<Integer, Integer> countRanks(Collection<Poker> cards) {
        Map<Integer, Integer> counts = new TreeMap<>();
        for (Poker card : cards) {
            counts.merge(card.getValue(), 1, Integer::sum);
        }
        return counts;
    }

    private static Map<Integer, List<Poker>> groupCards(Collection<Poker> cards) {
        Map<Integer, List<Poker>> grouped = new TreeMap<>();
        for (Poker card : cards) {
            grouped.computeIfAbsent(card.getValue(), ignored -> new ArrayList<>()).add(card);
        }
        return grouped;
    }

    private static boolean allCountsEqual(Map<Integer, Integer> counts, int expected) {
        return counts.values().stream().allMatch(count -> count == expected);
    }

    private static boolean isConsecutive(List<Integer> values, int maximum) {
        if (values.isEmpty() || values.get(values.size() - 1) > maximum) {
            return false;
        }
        for (int i = 1; i < values.size(); i++) {
            if (values.get(i) != values.get(i - 1) + 1) {
                return false;
            }
        }
        return true;
    }

    private static CardPattern pattern(CardPattern.Type type, int mainValue,
                                       int count, List<Integer> keyRanks) {
        return new CardPattern(type, mainValue, count, keyRanks);
    }
}
