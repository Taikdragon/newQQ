package src;

import javax.net.ssl.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.io.*;
import java.net.Socket;

public class JoinGui {
    private JFrame jFrame;
    private JPanel jPanel;
    private JTextField jTextField;
    private JPasswordField jPasswordField;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JoinGui().createAndShowGUI());
    }

    public void createAndShowGUI() {
        jFrame = new JFrame();
        jFrame.setTitle("QQ登录界面");
        jFrame.setSize(450, 400);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setLocationRelativeTo(null);

        jPanel = new JPanel();
        jPanel.setLayout(null);

        // 用户名输入框
        jTextField = new JTextField();
        jTextField.setBounds(140, 10, 180, 30);
        jPanel.add(jTextField);

        // 密码输入框
        jPasswordField = new JPasswordField();
        jPasswordField.setBounds(140, 50, 180, 30);
        jPanel.add(jPasswordField);

        // 标签
        JLabel QQhao = new JLabel("QQ号:");
        QQhao.setBounds(100, 10, 50, 20);
        jPanel.add(QQhao);

        JLabel mima = new JLabel("密码:");
        mima.setBounds(105, 50, 50, 20);
        jPanel.add(mima);

        // 复选框
        JCheckBox rememberPasswordCheckBox = new JCheckBox("记住密码");
        rememberPasswordCheckBox.setBounds(180, 80, 80, 20);
        jPanel.add(rememberPasswordCheckBox);

        JCheckBox autoLoginCheckBox = new JCheckBox("自动登录");
        autoLoginCheckBox.setBounds(180, 105, 80, 20);
        jPanel.add(autoLoginCheckBox);

        // 按钮
        JButton passwordRecoveryButton = new JButton("找回密码");
        passwordRecoveryButton.setBounds(330, 15, 85, 20);
        jPanel.add(passwordRecoveryButton);

        JButton registerButton = new JButton("注册账户");
        registerButton.setBounds(330, 55, 85, 20);
        jPanel.add(registerButton);

        JButton resetAccountButton = new JButton("重置账户");
        resetAccountButton.setBounds(330, 95, 85, 20);
        jPanel.add(resetAccountButton);

        // 登录按钮
        JButton loginButton = new JButton("登录");
        loginButton.setBounds(177, 200, 80, 37);
        loginButton.addActionListener(this::onLoginButtonClicked);
        jPanel.add(loginButton);

        // 按钮事件绑定
        registerButton.addActionListener(e -> showRegistrationForm());
        resetAccountButton.addActionListener(e -> new src.PasswordResetGui(jFrame).setVisible(true));
        passwordRecoveryButton.addActionListener(e -> new src.PasswordRecoveryGui(jFrame).setVisible(true));

        jFrame.add(jPanel);
        jFrame.setVisible(true);
    }

    private void onLoginButtonClicked(ActionEvent e) {
        String username = jTextField.getText();
        String password = new String(jPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(jPanel, "用户名或密码不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // 1. 创建信任所有证书的TrustManager
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };

            // 2. 初始化SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            // 3. 创建SSLSocketFactory
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            // 4. 建立SSL连接并发送登录请求
            try (Socket socket = sslSocketFactory.createSocket("192.168.0.103", 12345);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // 生成密码哈希
                String hashedPassword = src.PasswordUtil.hashPassword(password);
                out.println("LOGIN:" + username + ":" + hashedPassword);

                // 接收服务端响应
                String response = in.readLine();
                if (response != null && response.startsWith("SUCCESS")) {
                    JOptionPane.showMessageDialog(jPanel, "登录成功！", "信息", JOptionPane.INFORMATION_MESSAGE);
                    jFrame.dispose();
                    new src.ChatGui(username, socket).setVisible(true);
                } else {
                    String errorMsg = (response == null) ? "连接超时" : response.split(":")[1];
                    JOptionPane.showMessageDialog(jPanel, errorMsg, "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (IOException | NoSuchAlgorithmException | KeyManagementException ex) {
            JOptionPane.showMessageDialog(jPanel, "连接失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void showRegistrationForm() {
        new src.RegisterGui(jFrame).setVisible(true);
    }
}