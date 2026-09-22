package com.itheima.game;

import com.itheima.util.CodeUtil;
import com.itheima.util.ResourceUtil;
import com.itheima.util.UserStore;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * 登录界面，负责验证码、账号校验和打开注册窗口。
 */
public class LoginJFrame extends JFrame implements MouseListener {
    private final UserStore userStore = new UserStore();
    private final JTextField username = new JTextField();
    private final JPasswordField password = new JPasswordField();
    private final JTextField code = new JTextField();
    private final JLabel rightCode = new JLabel();
    private final JButton login = new JButton();
    private final JButton register = new JButton();
    private final JButton aiConfig = new JButton("AI设置");

    public LoginJFrame() {
        initJFrame();
        initView();
        setVisible(true);
    }

    private void initJFrame() {
        setSize(633, 423);
        setTitle("斗地主游戏 V1.0 登录");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        setLayout(null);
        setResizable(false);
    }

    private void initView() {
        Font labelFont = new Font("Microsoft YaHei", Font.BOLD, 16);

        JLabel background = new JLabel(ResourceUtil.loadImage("login/background.png"));
        background.setBounds(0, 0, 633, 423);
        getContentPane().add(background);

        JLabel usernameText = new JLabel("用户名");
        usernameText.setForeground(Color.WHITE);
        usernameText.setFont(labelFont);
        usernameText.setBounds(140, 55, 55, 22);
        background.add(usernameText);

        username.setBounds(223, 46, 200, 30);
        background.add(username);

        JLabel passwordText = new JLabel("密码");
        passwordText.setForeground(Color.WHITE);
        passwordText.setFont(labelFont);
        passwordText.setBounds(197, 95, 45, 22);
        background.add(passwordText);

        password.setBounds(263, 87, 160, 30);
        background.add(password);

        JLabel codeText = new JLabel("验证码");
        codeText.setForeground(Color.WHITE);
        codeText.setFont(labelFont);
        codeText.setBounds(215, 142, 65, 22);
        background.add(codeText);

        code.setBounds(291, 133, 100, 30);
        background.add(code);

        rightCode.setForeground(Color.RED);
        rightCode.setFont(new Font("Consolas", Font.BOLD, 17));
        rightCode.setHorizontalAlignment(JLabel.CENTER);
        rightCode.setBounds(400, 133, 100, 30);
        rightCode.addMouseListener(this);
        background.add(rightCode);
        refreshCode();

        login.setBounds(123, 310, 128, 47);
        login.setIcon(ResourceUtil.loadImage("login/登录按钮.png"));
        login.setBorderPainted(false);
        login.setContentAreaFilled(false);
        login.addMouseListener(this);
        background.add(login);

        register.setBounds(256, 310, 128, 47);
        register.setIcon(ResourceUtil.loadImage("login/注册按钮.png"));
        register.setBorderPainted(false);
        register.setContentAreaFilled(false);
        register.addMouseListener(this);
        background.add(register);

        aiConfig.setBounds(505, 16, 95, 30);
        aiConfig.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        aiConfig.addActionListener(e -> new ApiKeyDialog(this).setVisible(true));
        background.add(aiConfig);
    }

    private void refreshCode() {
        rightCode.setText(CodeUtil.getCode());
        code.setText("");
    }

    private void login() {
        String codeText = code.getText().trim();
        if (codeText.isEmpty()) {
            showMessage("验证码为空");
            return;
        }
        if (!codeText.equalsIgnoreCase(rightCode.getText())) {
            showMessage("验证码错误");
            refreshCode();
            return;
        }
        String usernameText = username.getText().trim();
        String passwordText = new String(password.getPassword());
        if (usernameText.isEmpty() || passwordText.isEmpty()) {
            showMessage("用户名或密码为空");
            refreshCode();
            return;
        }
        if (!userStore.authenticate(usernameText, passwordText)) {
            showMessage("用户名或密码错误");
            refreshCode();
            return;
        }
        Difficulty difficulty = DifficultyDialog.showDialog(this);
        if (difficulty == null) {
            return;
        }
        new GameJFrame(usernameText, difficulty);
        dispose();
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == rightCode) {
            refreshCode();
        } else if (e.getSource() == login) {
            login();
        } else if (e.getSource() == register) {
            new RegisterDialog(this, userStore).setVisible(true);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getSource() == login) {
            login.setIcon(ResourceUtil.loadImage("login/登录按下.png"));
        } else if (e.getSource() == register) {
            register.setIcon(ResourceUtil.loadImage("login/注册按下.png"));
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getSource() == login) {
            login.setIcon(ResourceUtil.loadImage("login/登录按钮.png"));
        } else if (e.getSource() == register) {
            register.setIcon(ResourceUtil.loadImage("login/注册按钮.png"));
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
