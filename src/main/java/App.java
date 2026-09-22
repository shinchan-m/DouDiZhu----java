import com.itheima.game.LoginJFrame;

import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginJFrame::new);
    }
}
