package com.itheima.game;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

/**
 * 登录成功后的难度选择窗口。
 */
public class DifficultyDialog extends JDialog {
    private Difficulty result;

    private DifficultyDialog(JFrame owner) {
        super(owner, "选择难度", true);
        initView();
        setSize(430, 250);
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    public static Difficulty showDialog(JFrame owner) {
        DifficultyDialog dialog = new DifficultyDialog(owner);
        dialog.setVisible(true);
        return dialog.result;
    }

    private void initView() {
        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(new Color(245, 242, 232));

        JLabel title = new JLabel("请选择 AI 难度", JLabel.CENTER);
        title.setFont(new Font("Microsoft YaHei", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(18, 0, 5, 0));
        add(title, BorderLayout.NORTH);

        JLabel hint = new JLabel("简单：本地策略    中等：标准 AI    困难：强化 AI",
                JLabel.CENTER);
        hint.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        hint.setForeground(new Color(80, 80, 80));
        add(hint, BorderLayout.SOUTH);

        JButton easy = createButton(Difficulty.EASY);
        JButton medium = createButton(Difficulty.MEDIUM);
        JButton hard = createButton(Difficulty.HARD);
        JButton[] buttons = {easy, medium, hard};

        javax.swing.JPanel buttonPanel = new javax.swing.JPanel(new GridLayout(1, 3, 12, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(25, 28, 22, 28));
        for (JButton button : buttons) {
            buttonPanel.add(button);
        }
        add(buttonPanel, BorderLayout.CENTER);
    }

    private JButton createButton(Difficulty difficulty) {
        JButton button = new JButton(difficulty.getDisplayName());
        button.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
        button.addActionListener(e -> {
            result = difficulty;
            dispose();
        });
        return button;
    }
}
