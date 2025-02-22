package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.Socket;

public class ChatGui extends JFrame {
    private static JTextArea messageArea;
    private JTextField inputField;
    private src.ChatClient client;
    private String username; // 新增字段保存用户名

    public ChatGui(String username, Socket socket) {
        this.username = username; // 初始化用户名
        setTitle("QQ聊天 - 欢迎 " + username);
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 连接服务器
        try {
            client = new src.ChatClient("192.168.0.103", 12345, username);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "连接异常", "错误", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

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

        // 发送消息时添加命令前缀
        Action sendAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = inputField.getText().trim();
                if (!message.isEmpty()) {
                    // 格式：CHAT:用户名:消息内容
                    client.sendMessage("CHAT:" + username + ":" + message);
                    appendMessage("我: " + message);
                    inputField.setText("");
                }
            }
        };

        sendButton.addActionListener(sendAction);
        inputField.addActionListener(sendAction);

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    public static void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            messageArea.append(message + "\n");
            messageArea.setCaretPosition(messageArea.getDocument().getLength());
        });
    }
}