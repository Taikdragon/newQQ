package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

public class ChatGui extends JFrame {
    private static JTextArea messageArea;
    private JTextField inputField;
    private src.ChatClient client;

    public ChatGui(String username) {
        setTitle("QQ聊天 - 欢迎 " + username);
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 连接服务器
        try {
            client = new src.ChatClient("localhost", 12345, username);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "无法连接到服务器", "错误", JOptionPane.ERROR_MESSAGE);
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

        // 确保发送按钮的事件监听器正确绑定
        Action sendAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = inputField.getText().trim();
                if (!message.isEmpty()) {
                    client.sendMessage(message); // 必须调用此方法
                    inputField.setText("");
                }
            }
        };
        // 检查按钮和输入框是否绑定同一个 Action
        sendButton.addActionListener(sendAction);
        inputField.addActionListener(sendAction);

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
            // 自动滚动到底部
            messageArea.setCaretPosition(messageArea.getDocument().getLength());
        });
    }

}