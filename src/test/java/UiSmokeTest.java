import com.itheima.game.GameJFrame;
import com.itheima.game.LoginJFrame;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class UiSmokeTest {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            LoginJFrame login = new LoginJFrame();
            login.dispose();
            GameJFrame game = new GameJFrame("smoke_user");
            Timer timer = new Timer(7000, e -> {
                game.dispose();
                System.out.println("UiSmokeTest passed");
                System.exit(0);
            });
            timer.setRepeats(false);
            timer.start();
        });
    }
}
