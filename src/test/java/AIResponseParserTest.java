import com.itheima.ai.AIAction;
import com.itheima.ai.AIResponseParser;
import com.itheima.domain.CardPattern;
import com.itheima.domain.Poker;
import com.itheima.rules.CardRules;

import java.util.ArrayList;
import java.util.List;

public class AIResponseParserTest {
    private static int passed;

    public static void main(String[] args) {
        List<Poker> hand = cards("3", "4", "5", "6", "7", "9", "9");

        AIAction play = AIResponseParser.parse(
                "{\"action\":\"play\",\"cards\":[\"3\",\"4\",\"5\",\"6\",\"7\"]}",
                hand, null, AIAction.Source.MODEL);
        check(play.isUsable() && !play.isPass(), "合法顺子应解析成功");

        AIAction markdown = AIResponseParser.parse(
                "```json\n{\"action\":\"pass\"}\n```",
                hand, CardRules.parse(cards("3", "3")), AIAction.Source.MODEL);
        check(!markdown.isUsable() && markdown.isRetryable(), "Markdown 包装应被拒绝");

        AIAction missingCard = AIResponseParser.parse(
                "{\"action\":\"play\",\"cards\":[\"A\"]}",
                hand, null, AIAction.Source.MODEL);
        check(!missingCard.isUsable() && missingCard.isRetryable(), "越权用牌应被拒绝");

        CardPattern pairFive = CardRules.parse(cards("5", "5"));
        AIAction tooSmall = AIResponseParser.parse(
                "{\"action\":\"play\",\"cards\":[\"4\"]}",
                hand, pairFive, AIAction.Source.MODEL);
        check(!tooSmall.isUsable(), "不能压过时应进入本地兜底");
        check(!tooSmall.isRetryable(), "无法压过不应再次请求模型");

        AIAction leadPass = AIResponseParser.parse(
                "{\"action\":\"pass\"}", hand, null, AIAction.Source.MODEL);
        check(!leadPass.isUsable() && !leadPass.isRetryable(),
                "自由领出时 pass 应直接走本地兜底");

        AIAction trailing = AIResponseParser.parse(
                "{\"action\":\"pass\"}{\"action\":\"pass\"}",
                hand, pairFive, AIAction.Source.MODEL);
        check(!trailing.isUsable() && trailing.isRetryable(), "多余 JSON 应被拒绝");

        System.out.println("AIResponseParserTest passed: " + passed);
    }

    private static List<Poker> cards(String... ranks) {
        List<Poker> result = new ArrayList<>();
        int[] suitUse = new int[18];
        for (String rank : ranks) {
            int value = valueOf(rank);
            if (value == 16) {
                result.add(new Poker("小王", "小王", 5, 1, 16));
                continue;
            }
            if (value == 17) {
                result.add(new Poker("大王", "大王", 5, 2, 17));
                continue;
            }
            int suitIndex = suitUse[value]++;
            result.add(new Poker(Poker.SUITS[suitIndex % 4], rank,
                    suitIndex % 4 + 1, Poker.imageIndexForRank(rank), value));
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
