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
    private static JList<String> userList;

    public ChatGui(String username, Socket socket) {
        this.username = username;
        setTitle("QQ聊天 - 欢迎 " + username);
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 初始化用户列表模型和组件
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
        JButton sendButton = new JButton("发送");
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
        fileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(ChatGui.this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    byte[] fileBytes = Files.readAllBytes(file.toPath());
                    String base64Content = Base64.getEncoder().encodeToString(fileBytes);
                    // 编码文件名
                    String encodedFileName = Base64.getEncoder().encodeToString(file.getName().getBytes());
                    client.sendMessage("FILE:" + username + ":" + encodedFileName + ":" + base64Content);
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

    /**
     * 追加消息到聊天区域（线程安全）
     */
    public static void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            messageArea.append(message + "\n");
            messageArea.setCaretPosition(messageArea.getDocument().getLength());
        });
    }

    /**
     * 更新在线用户列表（线程安全）
     */
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