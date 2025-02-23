package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Base64;
import java.nio.file.Files;

public class ChatGui extends JFrame {
    private static JTextArea messageArea;
    private JTextField inputField;
    private src.ChatClient client;
    private String username;
    private static DefaultListModel<String> userListModel;
    private JList<String> userList;

    public ChatGui(String username, Socket socket) {
        this.username = username;
        setTitle("QQ聊天 - 欢迎 " + username);
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            client = new src.ChatClient(socket, username);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "连接异常", "错误", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        JPanel mainPanel = new JPanel(new BorderLayout());

        // 消息显示区域
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 用户列表
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setPreferredSize(new Dimension(150, 0));
        mainPanel.add(userScrollPane, BorderLayout.EAST);

        // 输入面板
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        JButton sendButton = new JButton("发送");
        JButton fileButton = new JButton("发送文件");

        Action sendAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = inputField.getText().trim();
                if (!message.isEmpty()) {
                    client.sendMessage("CHAT:" + username + ":" + message);
                    appendMessage("我: " + message);
                    inputField.setText("");
                }
            }
        };

        fileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(ChatGui.this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    byte[] fileBytes = Files.readAllBytes(file.toPath());
                    String base64Content = Base64.getEncoder().encodeToString(fileBytes);
                    client.sendMessage("FILE:" + username + ":" + file.getName() + ":" + base64Content);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        sendButton.addActionListener(sendAction);
        inputField.addActionListener(sendAction);

        inputPanel.add(fileButton, BorderLayout.WEST);
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

    public static void updateUserList(String[] users) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String user : users) {
                userListModel.addElement(user);
            }
        });
    }
}