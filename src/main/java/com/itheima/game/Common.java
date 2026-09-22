package com.itheima.game;

import com.itheima.domain.Poker;

import javax.swing.SwingUtilities;
import java.awt.Point;

/**
 * 发牌动画工具。动画在后台线程执行，牌的位置更新回到 Swing EDT。
 */
public final class Common {
    private Common() {
    }

    public static void move(Poker poker, Point from, Point to) {
        if (from.equals(to)) {
            poker.setLocation(to);
            return;
        }
        int steps = 12;
        for (int step = 1; step <= steps; step++) {
            double ratio = step / (double) steps;
            int x = (int) Math.round(from.x + (to.x - from.x) * ratio);
            int y = (int) Math.round(from.y + (to.y - from.y) * ratio);
            try {
                SwingUtilities.invokeAndWait(() -> poker.setLocation(x, y));
                Thread.sleep(4);
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        try {
            SwingUtilities.invokeAndWait(() -> poker.setLocation(to));
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
    }
}
