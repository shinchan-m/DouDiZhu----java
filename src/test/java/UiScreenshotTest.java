import com.itheima.game.GameJFrame;
import com.itheima.game.LoginJFrame;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;

public class UiScreenshotTest {
    public static void main(String[] args) throws Exception {
        Robot robot = new Robot();
        File outputDir = new File("work/screenshots");
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IllegalStateException("无法创建截图目录");
        }

        LoginJFrame[] loginHolder = new LoginJFrame[1];
        SwingUtilities.invokeAndWait(() -> loginHolder[0] = new LoginJFrame());
        Thread.sleep(1000);
        capture(robot, loginHolder[0].getBounds(), new File(outputDir, "login.png"));
        SwingUtilities.invokeAndWait(loginHolder[0]::dispose);

        GameJFrame[] gameHolder = new GameJFrame[1];
        SwingUtilities.invokeAndWait(() -> gameHolder[0] = new GameJFrame("截图玩家"));
        Thread.sleep(5500);
        capture(robot, gameHolder[0].getBounds(), new File(outputDir, "game.png"));
        SwingUtilities.invokeAndWait(gameHolder[0]::dispose);
        System.out.println("UiScreenshotTest passed");
        System.exit(0);
    }

    private static void capture(Robot robot, Rectangle bounds, File output) throws Exception {
        BufferedImage image = robot.createScreenCapture(bounds);
        ImageIO.write(image, "png", output);
    }
}
