package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.TimerTask;
import java.util.Timer;


public class ChatGui extends JFrame {
    private static JTextArea messageArea;
    private static JTextField inputField;
    private src.ChatClient client;
    private String username;
    private static DefaultListModel<String> userListModel;
    private static JList<String> userList;
    private static JButton sendButton;
    private static Timer unmuteTimer;

    public ChatGui(String username, Socket socket) {
        this.username = username;
        setTitle("QQ聊天 - 欢迎 " + username);
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 初始化用户列表
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setPreferredSize(new Dimension(150, 0));

        try {
            client = new src.ChatClient(socket, username);
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

        // 用户列表添加到右侧
        mainPanel.add(userScrollPane, BorderLayout.EAST);

        // 输入面板
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        //JButton sendButton = new JButton("发送");
        sendButton = new JButton("发送");
        JButton fileButton = new JButton("发送文件");

        // 发送消息动作
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

        // 文件发送按钮事件
        // 文件发送按钮事件监听器
        fileButton.addActionListener(e -> {
            String targetUser = userList.getSelectedValue();
            if (targetUser == null) {
                JOptionPane.showMessageDialog(ChatGui.this, "请先选择接收文件的用户", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(ChatGui.this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    byte[] fileBytes = Files.readAllBytes(file.toPath());
                    String base64Content = Base64.getEncoder().encodeToString(fileBytes);
                    String encodedFileName = Base64.getEncoder().encodeToString(
                            file.getName().getBytes(StandardCharsets.UTF_8)
                    );

                    // 关键修改：添加 "FILE:" 前缀
                    String message = "FILE:" + targetUser + ":" + encodedFileName + ":" + base64Content;
                    client.sendMessage(message);

                    // 调试日志
                    System.out.println("[DEBUG] 发送文件消息: " + message);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // 绑定发送动作
        sendButton.addActionListener(sendAction);
        inputField.addActionListener(sendAction);

        // 输入面板布局
        inputPanel.add(fileButton, BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    public static void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> {

            if(message.startsWith("FILE:")) {
                handleFileMessage(message);
                return;
            }
            if(message.startsWith("SYSTEM:")){
                handleSystemMessage(message);
                return;
            }

            messageArea.append(message + "\n");
            messageArea.setCaretPosition(messageArea.getDocument().getLength());
        });
    }

    /**
     * 处理系统消息（如踢出通知）
     */
    private static void handleSystemMessage(String message) {
        String content = message.substring(7);

        // 1. 处理踢出消息
        if (content.contains("你已被管理员踢出")) {
            JOptionPane.showMessageDialog(null, content, "警告", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        // 2. 处理禁言消息
        else if (content.contains("你已被禁言")) {
            int minutes = Integer.parseInt(content.replaceAll("[^0-9]", ""));
            JOptionPane.showMessageDialog(null, content, "系统通知", JOptionPane.WARNING_MESSAGE);

            // 禁用输入框和发送按钮
            inputField.setEnabled(false);
            sendButton.setEnabled(false);

            // 取消之前的定时器（防止重复）
            if (unmuteTimer != null) {
                unmuteTimer.cancel();
                unmuteTimer = null; // 重置引用
            }

            // 创建新定时器（使用 java.util.Timer）
            unmuteTimer = new Timer();
            unmuteTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    SwingUtilities.invokeLater(() -> {
                        inputField.setEnabled(true);
                        sendButton.setEnabled(true);
                        appendMessage("SYSTEM:禁言已解除，可以正常发言");
                    });
                }
            }, minutes * 60 * 1000);
        }

        // 3. 处理服务端发送的禁言解除通知
        else if (content.contains("的禁言已解除")) {
            SwingUtilities.invokeLater(() -> {
                inputField.setEnabled(true);
                sendButton.setEnabled(true);
                appendMessage("SYSTEM:禁言已解除，可以正常发言");
            });
        }

        // 4. 其他系统消息
        else {
            messageArea.append("[系统] " + content + "\n");
        }
    }


    private static void handleFileMessage(String message) {
        try {
            String[] parts = message.split(":", 5); // 格式: FILE:发送者:文件名(base64):内容(base64)
            String sender = parts[1];
            String targetUser = parts[2];
            String encodedFileName = parts[3];
            String fileContent = parts[4];

            // 解码文件名和内容
            String fileName = new String(
                    Base64.getDecoder().decode(encodedFileName),
                    StandardCharsets.UTF_8
            );
            byte[] fileBytes = Base64.getDecoder().decode(fileContent);

            // 弹出文件保存对话框
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(fileName));
            int userChoice = fileChooser.showSaveDialog(null);

            if (userChoice == JFileChooser.APPROVE_OPTION) {
                File saveFile = fileChooser.getSelectedFile();
                Files.write(saveFile.toPath(), fileBytes);
                messageArea.append("[系统] 文件已保存至: " + saveFile.getAbsolutePath() + "\n");
            }
        } catch (Exception e) {
            messageArea.append("[错误] 文件接收失败: " + e.getMessage() + "\n");
        }
    }

    public static void updateUserList(String[] users) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String user : users) {
                if (!user.isEmpty()) {
                    userListModel.addElement(user);
                }
            }
        });
    }
}