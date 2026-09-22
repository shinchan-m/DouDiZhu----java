import com.itheima.domain.Poker;

import java.util.Arrays;
import java.util.List;

public class PokerOrderTest {
    public static void main(String[] args) {
        List<String> ranks = Arrays.asList(
                "3", "4", "5", "6", "7", "8", "9", "10",
                "J", "Q", "K", "A", "2", "小王", "大王");
        int[] expectedValues = {
                3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17
        };
        for (int i = 0; i < ranks.size(); i++) {
            int actual = Poker.rankValue(ranks.get(i));
            if (actual != expectedValues[i]) {
                throw new AssertionError(ranks.get(i) + " 的比较值错误: " + actual);
            }
        }

        checkImage("A", 1);
        checkImage("2", 2);
        checkImage("3", 3);
        checkImage("10", 10);
        checkImage("J", 11);
        checkImage("Q", 12);
        checkImage("K", 13);

        List<Poker> deck = Poker.createDeck();
        if (deck.size() != 54) {
            throw new AssertionError("牌盒不是 54 张: " + deck.size());
        }
        System.out.println("PokerOrderTest passed");
    }

    private static void checkImage(String rank, int expected) {
        int actual = Poker.imageIndexForRank(rank);
        if (actual != expected) {
            throw new AssertionError(rank + " 的图片编号错误: " + actual);
        }
    }
}
