package com.itheima.game;

import com.itheima.util.UserStore;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Font;

/**
 * 注册对话框，只接受未注册且校验通过的用户名密码。
 */
public class RegisterDialog extends JDialog {
    private final UserStore userStore;
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JPasswordField confirmField = new JPasswordField();

    public RegisterDialog(JFrame owner, UserStore userStore) {
        super(owner, "注册账号", true);
        this.userStore = userStore;
        initView();
        setSize(400, 300);
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void initView() {
        setLayout(null);
        getContentPane().setBackground(new Color(245, 245, 240));
        Font font = new Font("Microsoft YaHei", Font.PLAIN, 15);

        JLabel title = new JLabel("注册新账号");
        title.setFont(new Font("Microsoft YaHei", Font.BOLD, 22));
        title.setBounds(135, 18, 150, 32);
        add(title);

        addLabel("用户名", 45, 75, font);
        usernameField.setBounds(145, 70, 200, 30);
        add(usernameField);

        addLabel("密码", 45, 120, font);
        passwordField.setBounds(145, 115, 200, 30);
        add(passwordField);

        addLabel("确认密码", 45, 165, font);
        confirmField.setBounds(145, 160, 200, 30);
        add(confirmField);

        JButton registerButton = new JButton("注册");
        registerButton.setBounds(85, 220, 90, 34);
        registerButton.addActionListener(e -> register());
        add(registerButton);

        JButton cancelButton = new JButton("取消");
        cancelButton.setBounds(225, 220, 90, 34);
        cancelButton.addActionListener(e -> dispose());
        add(cancelButton);
    }

    private void addLabel(String text, int x, int y, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setBounds(x, y, 100, 30);
        add(label);
    }

    private void register() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());
        if (username.isEmpty()) {
            showMessage("用户名不能为空");
            return;
        }
        if (userStore.usernameExists(username)) {
            showMessage("用户名已存在");
            return;
        }
        if (password.isEmpty()) {
            showMessage("密码不能为空");
            return;
        }
        if (!password.equals(confirm)) {
            showMessage("两次输入的密码不一致");
            return;
        }
        try {
            userStore.register(username, password);
            showMessage("注册成功");
            dispose();
        } catch (IllegalArgumentException | IllegalStateException e) {
            showMessage(e.getMessage());
        }
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}
