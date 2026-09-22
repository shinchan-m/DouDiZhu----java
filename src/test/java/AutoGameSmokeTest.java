import com.itheima.game.GameJFrame;
import com.itheima.game.Difficulty;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

public class AutoGameSmokeTest {
    public static void main(String[] args) throws Exception {
        System.setProperty("doudizhu.test.autoClose", "true");
        Difficulty difficulty = args.length == 0
                ? Difficulty.MEDIUM
                : Difficulty.valueOf(args[0]);
        final GameJFrame[] frameHolder = new GameJFrame[1];
        SwingUtilities.invokeAndWait(() ->
                frameHolder[0] = new GameJFrame("auto_test", difficulty));

        Class<?> frameClass = GameJFrame.class;
        Field phaseField = frameClass.getDeclaredField("phase");
        Field currentField = frameClass.getDeclaredField("currentPlayer");
        Field gameOverField = frameClass.getDeclaredField("gameOver");
        Method bidMethod = frameClass.getDeclaredMethod("recordBid", boolean.class);
        Method timeoutMethod = frameClass.getDeclaredMethod("humanTimeout");
        for (Field field : new Field[]{phaseField, currentField, gameOverField}) {
            field.setAccessible(true);
        }
        bidMethod.setAccessible(true);
        timeoutMethod.setAccessible(true);

        AtomicLong started = new AtomicLong(System.currentTimeMillis());
        Timer timer = new Timer(100, e -> {
            try {
                GameJFrame frame = frameHolder[0];
                if ((boolean) gameOverField.get(frame)) {
                    ((Timer) e.getSource()).stop();
                    System.out.println("AutoGameSmokeTest passed");
                    System.exit(0);
                }
                if (System.currentTimeMillis() - started.get() > 45_000) {
                    ((Timer) e.getSource()).stop();
                    throw new AssertionError("整局测试超时");
                }
                String phase = phaseField.get(frame).toString();
                int current = currentField.getInt(frame);
                if (current != 1) {
                    return;
                }
                if ("BIDDING".equals(phase)) {
                    bidMethod.invoke(frame, true);
                } else if ("PLAYING".equals(phase)) {
                    timeoutMethod.invoke(frame);
                }
            } catch (Exception ex) {
                ((Timer) e.getSource()).stop();
                ex.printStackTrace();
                System.exit(1);
            }
        });
        timer.start();
    }
}
