import com.itheima.game.GameJFrame;

import javax.swing.SwingUtilities;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

public class CountdownSmokeTest {
    public static void main(String[] args) throws Exception {
        System.setProperty("doudizhu.test.autoClose", "true");
        GameJFrame[] frameHolder = new GameJFrame[1];
        SwingUtilities.invokeAndWait(() -> frameHolder[0] = new GameJFrame("countdown_test"));

        Method startCountdown = GameJFrame.class.getDeclaredMethod(
                "startCountdown", int.class, Runnable.class);
        startCountdown.setAccessible(true);
        AtomicBoolean fired = new AtomicBoolean(false);
        SwingUtilities.invokeAndWait(() -> {
            try {
                startCountdown.invoke(frameHolder[0], 1, (Runnable) () -> fired.set(true));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Thread.sleep(1800);
        SwingUtilities.invokeAndWait(frameHolder[0]::dispose);
        if (!fired.get()) {
            throw new AssertionError("倒计时归零后没有触发超时动作");
        }
        System.out.println("CountdownSmokeTest passed");
        System.exit(0);
    }
}
