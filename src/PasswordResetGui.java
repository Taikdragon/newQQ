package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class PasswordResetGui extends JDialog {
    private JTextField usernameField;
    private JPasswordField newPasswordField;
    private JLabel statusLabel;


    public PasswordResetGui(JFrame parent) {
        super(parent, "重置密码", true);
        setSize(350, 250);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 用户名输入
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userPanel.add(new JLabel("用户名:"));
        usernameField = new JTextField(15);
        userPanel.add(usernameField);
        panel.add(userPanel);

        // 新密码输入
        JPanel pwdPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pwdPanel.add(new JLabel("新密码:"));
        newPasswordField = new JPasswordField(15);
        pwdPanel.add(newPasswordField);
        panel.add(pwdPanel);

        // 重置按钮
        JButton resetButton = new JButton("重置密码");
        resetButton.addActionListener(this::onReset);
        panel.add(resetButton);

        // 状态提示
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        panel.add(statusLabel);

        add(panel);
    }

    /*
    private void onReset(ActionEvent e) {
        String username = usernameField.getText().trim();
        String newPassword = new String(newPasswordField.getPassword());

        if (username.isEmpty() || newPassword.isEmpty()) {
            showError("所有字段必须填写");
            return;
        }

        try {
            if (src.UserStorage.resetPassword(username, newPassword)) {
                statusLabel.setText("密码重置成功");
                statusLabel.setForeground(Color.BLUE);
            } else {
                showError("用户不存在");
            }
        } catch (Exception ex) {
            showError("系统错误，请重试");
            ex.printStackTrace();
        }
    }
    */

    private void onReset(ActionEvent e) {
        String username = usernameField.getText().trim();
        String newPassword = new String(newPasswordField.getPassword());

        try {
            // 连接服务端
            Socket socket = new Socket("192.168.0.103", 12345);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 发送重置命令：RESET_PASSWORD:用户名:新密码哈希
            String newHash = src.UserStorage.hashPassword(newPassword);
            out.println("RESET_PASSWORD:" + username + ":" + newHash);

            // 接收服务端响应
            String response = in.readLine();
            if (response.startsWith("SUCCESS")) {
                statusLabel.setText("密码重置成功");
                statusLabel.setForeground(Color.BLUE);
            } else {
                statusLabel.setText(response.split(":")[1]);
                statusLabel.setForeground(Color.RED);
            }

            socket.close();
        } catch (IOException | NoSuchAlgorithmException ex) {
            showError("连接服务端失败");
            ex.printStackTrace();
        }
    }

    private void showError(String msg) {
        statusLabel.setText(msg);
        statusLabel.setForeground(Color.RED);
    }
}