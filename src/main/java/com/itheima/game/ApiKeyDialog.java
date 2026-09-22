package com.itheima.game;

import com.itheima.util.ApiKeyStore;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

/**
 * 在游戏内设置 DeepSeek API Key，用户不需要直接打开 .key 文件。
 */
public class ApiKeyDialog extends JDialog {
    private final JPasswordField keyField = new JPasswordField();

    public ApiKeyDialog(JFrame owner) {
        super(owner, "DeepSeek AI 设置", true);
        initView();
        setSize(500, 250);
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void initView() {
        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(new Color(245, 242, 232));

        JLabel title = new JLabel("设置 DeepSeek API Key", JLabel.CENTER);
        title.setFont(new Font("Microsoft YaHei", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        javax.swing.JPanel form = new javax.swing.JPanel(new GridLayout(3, 1, 6, 6));
        form.setOpaque(false);
        form.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 28, 8, 28));

        JLabel hint = new JLabel("Key 只保存在本机，不会发送到其他位置。");
        hint.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        form.add(hint);

        keyField.setText(ApiKeyStore.load());
        keyField.setFont(new Font("Consolas", Font.PLAIN, 15));
        form.add(keyField);

        JCheckBox showKey = new JCheckBox("显示 API Key");
        showKey.setOpaque(false);
        showKey.addActionListener(e ->
                keyField.setEchoChar(showKey.isSelected() ? (char) 0 : '•'));
        form.add(showKey);
        add(form, BorderLayout.CENTER);

        JButton save = new JButton("保存");
        JButton clear = new JButton("清除");
        JButton cancel = new JButton("取消");
        save.addActionListener(e -> saveKey());
        clear.addActionListener(e -> clearKey());
        cancel.addActionListener(e -> dispose());

        javax.swing.JPanel buttons = new javax.swing.JPanel(new GridLayout(1, 3, 12, 0));
        buttons.setOpaque(false);
        buttons.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 45, 18, 45));
        buttons.add(save);
        buttons.add(clear);
        buttons.add(cancel);
        add(buttons, BorderLayout.SOUTH);
    }

    private void saveKey() {
        String key = new String(keyField.getPassword()).trim();
        if (key.isEmpty()) {
            JOptionPane.showMessageDialog(this, "API Key 不能为空");
            return;
        }
        try {
            ApiKeyStore.save(key);
            JOptionPane.showMessageDialog(this, "保存成功");
            dispose();
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void clearKey() {
        try {
            ApiKeyStore.clear();
            keyField.setText("");
            JOptionPane.showMessageDialog(this, "已清除本地 API Key");
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}
