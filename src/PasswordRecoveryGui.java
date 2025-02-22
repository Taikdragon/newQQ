package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;

public class PasswordRecoveryGui extends JDialog {
    private JTextField usernameField;
    private JLabel resultLabel;
    private static final String SERVER_IP = "192.168.0.103"; // 服务端 IP
    private static final int SERVER_PORT = 12345;            // 服务端端口

    public PasswordRecoveryGui(JFrame parent) {
        super(parent, "找回密码", true);
        setSize(350, 200);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 用户名输入
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("用户名:"));
        usernameField = new JTextField(15);
        inputPanel.add(usernameField);
        panel.add(inputPanel);

        // 查询按钮
        JButton searchButton = new JButton("查询");
        searchButton.addActionListener(this::onSearch);
        panel.add(searchButton);

        // 结果显示
        resultLabel = new JLabel(" ", SwingConstants.CENTER);
        panel.add(resultLabel);

        add(panel);
    }

    private void onSearch(ActionEvent e) {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            updateResultLabel("用户名不能为空！", Color.RED);
            return;
        }

        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // 发送找回密码命令: FIND_PASSWORD:用户名
            out.println("FIND_PASSWORD:" + username + ":");

            // 接收服务端响应
            String response = in.readLine();
            if (response.startsWith("SUCCESS")) {
                String email = response.split(":")[1].trim();
                updateResultLabel("安全邮箱: " + email, Color.BLUE);
            } else {
                updateResultLabel(response.split(":")[1], Color.RED);
            }

        } catch (IOException ex) {
            updateResultLabel("连接服务端失败", Color.RED);
            ex.printStackTrace();
        }
    }

    // 线程安全的界面更新方法
    private void updateResultLabel(String text, Color color) {
        SwingUtilities.invokeLater(() -> {
            resultLabel.setText(text);
            resultLabel.setForeground(color);
        });
    }
}