import com.itheima.domain.CardPattern;
import com.itheima.domain.Poker;
import com.itheima.rules.CardRules;

import java.util.ArrayList;
import java.util.List;

public class CardRulesTest {
    private static int passed;

    public static void main(String[] args) {
        expectType(CardPattern.Type.SINGLE, cards("3"));
        expectType(CardPattern.Type.PAIR, cards("4", "4"));
        expectType(CardPattern.Type.TRIPLE, cards("5", "5", "5"));
        expectType(CardPattern.Type.TRIPLE_ONE, cards("6", "6", "6", "9"));
        expectType(CardPattern.Type.TRIPLE_TWO, cards("7", "7", "7", "3", "A"));
        expectType(CardPattern.Type.STRAIGHT, cards("3", "4", "5", "6", "7"));
        expectType(CardPattern.Type.PAIR_STRAIGHT,
                cards("3", "3", "4", "4", "5", "5"));
        expectType(CardPattern.Type.PLANE,
                cards("3", "3", "3", "4", "4", "4"));
        expectType(CardPattern.Type.PLANE_SINGLE,
                cards("3", "3", "3", "4", "4", "4", "7", "9"));
        expectType(CardPattern.Type.PLANE_PAIR,
                cards("3", "3", "3", "4", "4", "4", "7", "7", "9", "9"));
        expectType(CardPattern.Type.BOMB, cards("8", "8", "8", "8"));
        expectType(CardPattern.Type.ROCKET, cards("小王", "大王"));
        expectType(null, cards("3", "4", "5", "6", "7", "2"));
        expectType(null, cards("3", "3", "4", "4"));
        expectType(null, cards("3", "3", "3", "3", "4", "4"));

        CardPattern pairFour = CardRules.parse(cards("4", "4"));
        CardPattern pairFive = CardRules.parse(cards("5", "5"));
        CardPattern bombThree = CardRules.parse(cards("3", "3", "3", "3"));
        CardPattern rocket = CardRules.parse(cards("小王", "大王"));
        check(CardRules.canBeat(pairFive, pairFour), "更大的对子应该能压过");
        check(CardRules.canBeat(bombThree, pairFive), "炸弹应该能压过普通牌型");
        check(!CardRules.canBeat(pairFive, bombThree), "普通牌不能压炸弹");
        check(CardRules.canBeat(rocket, bombThree), "王炸应该最大");

        List<Poker> hand = cards("3", "3", "4", "4", "5", "5", "6", "7", "8", "9", "10");
        check(!CardRules.findAllLegalPlays(hand, null).isEmpty(), "应能生成合法出牌");

        System.out.println("CardRulesTest passed: " + passed);
    }

    private static void expectType(CardPattern.Type expected, List<Poker> cards) {
        CardPattern pattern = CardRules.parse(cards);
        if (expected == null) {
            check(pattern == null, "应判定为非法牌型: " + cards);
        } else {
            check(pattern != null && pattern.type() == expected,
                    "牌型错误，期望 " + expected + "，实际 " + pattern + ": " + cards);
        }
    }

    private static List<Poker> cards(String... ranks) {
        List<Poker> result = new ArrayList<>();
        int[] counts = new int[18];
        for (String rank : ranks) {
            int value = valueOf(rank);
            counts[value]++;
            if (value == 16) {
                result.add(new Poker("小王", "小王", 5, 1, 16));
            } else if (value == 17) {
                result.add(new Poker("大王", "大王", 5, 2, 17));
            } else {
                result.add(new Poker(Poker.SUITS[counts[value] % 4],
                        rank, counts[value] % 4 + 1,
                        Poker.imageIndexForRank(rank), value));
            }
        }
        return result;
    }

    private static int valueOf(String rank) {
        return switch (rank) {
            case "J" -> 11;
            case "Q" -> 12;
            case "K" -> 13;
            case "A" -> 14;
            case "2" -> 15;
            case "小王" -> 16;
            case "大王" -> 17;
            default -> Integer.parseInt(rank);
        };
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
        passed++;
    }
}
