package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.TimerTask;
import java.util.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import java.util.List;
import java.util.Arrays;
import java.util.Comparator;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.nio.file.StandardCopyOption;

public class ChatGui extends JFrame {
    // 修改 messageArea 类型为 JTextPane
    private static JTextPane messageArea;
    private static JTextField inputField;
    private src.ChatClient client;
    private String username;
    private static DefaultListModel<String> userListModel;
    private static JList<String> userList;
    private static JButton sendButton;
    private static Timer unmuteTimer;
    private JDialog emojiDialog;
    private JPanel emojiPanel;
    private JButton addEmojiButton;

    public ChatGui(String username, Socket socket) {
        this.username = username;
        setTitle("QQ聊天 - 欢迎 " + username);
        // 增大窗口大小
        setSize(900, 500); 
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
        // 修改前
        // messageArea = new JTextArea();
        // 修改后
        messageArea = new JTextPane();
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

        // 新增表情管理按钮
        JButton emojiButton = new JButton("表情");
        //inputPanel.add(emojiButton, BorderLayout.WEST);

        // 新增表情包预览区域
        /*
        JPanel emojiPanel = new JPanel(new FlowLayout());
        JScrollPane emojiScrollPane = new JScrollPane(emojiPanel);
        emojiScrollPane.setPreferredSize(new Dimension(150, 100));
        emojiScrollPane.setVisible(false);
        mainPanel.add(emojiScrollPane, BorderLayout.NORTH);

         */

        // 强制更新布局
        mainPanel.revalidate();
        mainPanel.repaint();

        // 表情管理按钮事件监听器
        /*
        emojiButton.addActionListener(e -> {
            System.out.println("表情按钮被点击");
            emojiScrollPane.setVisible(!emojiScrollPane.isVisible());
            if (emojiScrollPane.isVisible()) {
                // Bug修复: 去掉emojiPanel参数
                loadEmojis(); 
            }
            // 确保组件被正确重绘
            emojiScrollPane.revalidate();
            emojiScrollPane.repaint();
            // 输出组件的大小和位置信息
            System.out.println("emojiScrollPane bounds: " + emojiScrollPane.getBounds());
        });

         */

        emojiButton.addActionListener(e -> {
            if (emojiDialog == null) {
                initEmojiDialog(); // 初始化对话框
            }
            refreshEmojiList();    // 刷新表情列表
            emojiDialog.setVisible(true); // 显示对话框
        });

        // 发送消息动作
        // 修改后的发送消息动作
        Action sendAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = inputField.getText().trim();
                if (message.startsWith("/")) {
                    if (message.equals("/help")) {
                        showHelpDialog();
                        return;
                    } else if (message.startsWith("/changepwd ")) {
                        String newPwd = message.substring(11);
                        if (newPwd.isEmpty()) {
                            JOptionPane.showMessageDialog(ChatGui.this, "新密码不能为空", "错误", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        try {
                            // 捕获 NoSuchAlgorithmException
                            String hashedPwd = src.PasswordUtil.hashPassword(newPwd);
                            client.sendMessage("RESET_PASSWORD:" + username + ":" + hashedPwd);
                        } catch (NoSuchAlgorithmException ex) {
                            JOptionPane.showMessageDialog(ChatGui.this,
                                    "密码加密失败：SHA-256算法不可用",
                                    "错误",
                                    JOptionPane.ERROR_MESSAGE);
                            ex.printStackTrace();
                        }
                        return;
                    } else if (message.startsWith("/who")) {
                        client.sendMessage("/who");
                    } else if (message.startsWith("/history ")) {
                        client.sendMessage(message);
                    } else {
                        JOptionPane.showMessageDialog(ChatGui.this, "未知命令", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                    return;
                } else if (!message.isEmpty()) {
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
        inputPanel.add(emojiButton, BorderLayout.AFTER_LAST_LINE);

        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void initEmojiDialog() {
        emojiDialog = new JDialog(this, "表情包", false);
        emojiDialog.setSize(600, 400);
        emojiDialog.setLocationRelativeTo(this);

        // 主面板布局
        emojiPanel = new JPanel(new GridLayout(0, 8, 5, 5)); // 8列网格
        JScrollPane scrollPane = new JScrollPane(emojiPanel);

        // 控制面板
        JPanel controlPanel = new JPanel();
        addEmojiButton = new JButton("添加表情");
        addEmojiButton.addActionListener(this::onAddEmoji);
        controlPanel.add(addEmojiButton);

        emojiDialog.add(scrollPane, BorderLayout.CENTER);
        emojiDialog.add(controlPanel, BorderLayout.SOUTH);
    }


    // 修改 appendMessage 方法中处理 JTextPane 的部分
    public static void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            if (message.startsWith("USERS:")) {
                String[] users = message.split(":")[1].split(",");
                updateUserList(users);
                return;
            }
            if (message.startsWith("FILE:")) {
                handleFileMessage(message);
                return;
            }
            if (message.startsWith("SYSTEM:")) {
                handleSystemMessage(message);
                return;
            }
            if (message.startsWith("HISTORY:")) {
                String history = message.split(":", 2)[1];
                messageArea.setText(messageArea.getText() + "\n=== 历史消息 ===\n" + history + "=================\n");
                return;
            }
            if (message.startsWith("EMOJI:")) {
                String[] parts = message.split(":", 3); // 格式：EMOJI:发送者:文件名
                if (parts.length < 3) return;

                String sender = parts[1];
                String emojiFileName = parts[2];

                try {
                    File emojiFile = new File("emojis/" + emojiFileName);
                    if (emojiFile.exists()) {
                        ImageIcon icon = new ImageIcon(emojiFile.getAbsolutePath());
                        Image scaledImage = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
                        ImageIcon scaledIcon = new ImageIcon(scaledImage);

                        // 显示发送者信息 + 表情
                        messageArea.setCaretPosition(messageArea.getDocument().getLength());
                        messageArea.insertIcon(scaledIcon);
                        messageArea.replaceSelection(" [" + sender + "的表情]\n");
                    }
                } catch (Exception ex) {
                    messageArea.setText(messageArea.getText() + "[表情加载失败]\n");
                }
                return;
            }
            messageArea.setText(messageArea.getText() + message + "\n");
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

            // 取消之前的定时器（防止重复）666
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
                if (unmuteTimer != null) {
                    unmuteTimer.cancel();
                    unmuteTimer = null;
                }
            });
        }

        // 4. 其他系统消息
        else {
            try {
                messageArea.getStyledDocument().insertString(messageArea.getStyledDocument().getLength(), "[系统] " + content + "\n", null);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    private void showHelpDialog() {
        String helpText = """
        可用命令：
        /help - 显示帮助
        /who - 查看在线用户
        /changepwd <新密码> - 修改密码
        /history <条数> - 查看历史消息
        """;
        JOptionPane.showMessageDialog(this, helpText, "帮助", JOptionPane.INFORMATION_MESSAGE);
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
                try {
                    messageArea.getStyledDocument().insertString(messageArea.getStyledDocument().getLength(), "[系统] 文件已保存至: " + saveFile.getAbsolutePath() + "\n", null);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            try {
                messageArea.getStyledDocument().insertString(messageArea.getStyledDocument().getLength(), "[错误] 文件接收失败: " + e.getMessage() + "\n", null);
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
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

    private void loadEmojis() {
        emojiDialog = new JDialog(this, "表情包", false);
        emojiDialog.setSize(600, 400);
        emojiDialog.setLocationRelativeTo(this);
    
        emojiPanel = new JPanel(new GridLayout(0, 8, 5, 5)); // 8列网格布局
        JScrollPane scrollPane = new JScrollPane(emojiPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    
        // 添加表情按钮面板
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addEmojiButton = new JButton("添加表情");
        addEmojiButton.addActionListener(this::onAddEmoji);
        controlPanel.add(addEmojiButton);
    
        // 加载现有表情
        refreshEmojiList();
    
        emojiDialog.add(scrollPane, BorderLayout.CENTER);
        emojiDialog.add(controlPanel, BorderLayout.SOUTH);
    }

        // 新增刷新表情列表方法
    private void refreshEmojiList() {
        emojiPanel.removeAll();
        File emojiFolder = new File("emojis");
        if (!emojiFolder.exists()) emojiFolder.mkdirs();

        List<File> emojiFiles = Arrays.asList(emojiFolder.listFiles());
        emojiFiles.sort(Comparator.comparing(File::getName)); // 按文件名排序

        for (File file : emojiFiles) {
            if (isImageFile(file.getName())) {
                try {
                    ImageIcon icon = new ImageIcon(file.getPath());
                    Image scaled = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                    JButton btn = new JButton(new ImageIcon(scaled));
                    btn.setToolTipText(file.getName());
                    btn.addActionListener(e -> sendEmoji(file));
                    emojiPanel.add(btn);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        emojiPanel.revalidate();
        emojiPanel.repaint();
    }

    // 新增发送表情方法
    private void sendEmoji(File emojiFile) {
        try {
            String emojiName = emojiFile.getName();
            // 修正协议格式：添加发送者信息
            String message = "EMOJI:" + username + ":" + emojiName;
            client.sendMessage(message);
            appendMessage("我: [表情]"); // 本地显示简略提示
            emojiDialog.setVisible(false);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "表情发送失败", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 新增添加表情事件处理
    private void onAddEmoji(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择表情图片");
        fileChooser.setFileFilter(new FileNameExtensionFilter("图片文件", "jpg", "png", "gif"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            previewAndSaveEmoji(selectedFile);
        }
    }

    // 新增预览保存方法
    private void previewAndSaveEmoji(File sourceFile) {
        JDialog previewDialog = new JDialog(this, "预览表情", true);
        previewDialog.setSize(300, 300);

        try {
            ImageIcon icon = new ImageIcon(sourceFile.getPath());
            Image scaled = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            JLabel previewLabel = new JLabel(new ImageIcon(scaled));

            JButton saveButton = new JButton("保存为表情");
            saveButton.addActionListener(e -> {
                try {
                    File dest = new File("emojis/" + sourceFile.getName());
                    Files.copy(sourceFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    refreshEmojiList();
                    previewDialog.dispose();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "保存失败: " + ex.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
                }
            });

            previewDialog.add(previewLabel, BorderLayout.CENTER);
            previewDialog.add(saveButton, BorderLayout.SOUTH);
            previewDialog.setLocationRelativeTo(this);
            previewDialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "图片加载失败", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }



    private boolean isImageFile(String fileName) {
        String lower = fileName.toLowerCase();
        return lower.endsWith(".jpg") ||
                lower.endsWith(".jpeg") ||
                lower.endsWith(".png") ||
                lower.endsWith(".gif"); // 增加gif支持
    }
}