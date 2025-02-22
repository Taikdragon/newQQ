package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatGui extends JFrame {
    private JTextArea messageArea;
    private JTextField inputField;

    public ChatGui(String username) {
        setTitle("QQ聊天 - 欢迎 " + username);
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 主面板布局
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 消息显示区域
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 输入面板
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        JButton sendButton = new JButton("发送");

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage()); // 回车发送

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            messageArea.append("我: " + message + "\n");
            inputField.setText("");
        }
    }
}