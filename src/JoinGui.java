package src;

import javax.net.ssl.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Properties;

public class JoinGui {
    private JFrame jFrame;
    private JPanel jPanel;
    private JTextField jTextField;
    private JPasswordField jPasswordField;
    private JCheckBox rememberPasswordCheckBox;
    private JCheckBox autoLoginCheckBox;
    private JLabel loginStatusLabel;
    private JButton cancelAutoLoginButton;
    private Timer loginTimer;

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

        jTextField = new JTextField();
        jTextField.setBounds(140, 10, 180, 30);
        jPanel.add(jTextField);

        jPasswordField = new JPasswordField();
        jPasswordField.setBounds(140, 50, 180, 30);
        jPanel.add(jPasswordField);

        JLabel QQhao = new JLabel("QQ号:");
        QQhao.setBounds(100, 10, 50, 20);
        jPanel.add(QQhao);

        JLabel mima = new JLabel("密码:");
        mima.setBounds(105, 50, 50, 20);
        jPanel.add(mima);

        rememberPasswordCheckBox = new JCheckBox("记住密码");
        rememberPasswordCheckBox.setBounds(180, 80, 80, 20);
        jPanel.add(rememberPasswordCheckBox);

        autoLoginCheckBox = new JCheckBox("自动登录");
        autoLoginCheckBox.setBounds(180, 105, 80, 20);
        jPanel.add(autoLoginCheckBox);

        JButton passwordRecoveryButton = new JButton("找回密码");
        passwordRecoveryButton.setBounds(330, 15, 85, 20);
        jPanel.add(passwordRecoveryButton);

        JButton registerButton = new JButton("注册账户");
        registerButton.setBounds(330, 55, 85, 20);
        jPanel.add(registerButton);

        JButton resetAccountButton = new JButton("重置账户");
        resetAccountButton.setBounds(330, 95, 85, 20);
        jPanel.add(resetAccountButton);

        JButton IPButton = new JButton("服务器IP");
        // 调整 IP 按钮的位置，避免与取消自动登录按钮重叠
        IPButton.setBounds(175, 310, 85, 20); 
        jPanel.add(IPButton);

        JButton loginButton = new JButton("登录");
        loginButton.setBounds(177, 200, 80, 37);
        loginButton.addActionListener(this::onLoginButtonClicked);
        jPanel.add(loginButton);

        registerButton.addActionListener(e -> showRegistrationForm());
        resetAccountButton.addActionListener(e -> new src.PasswordResetGui(jFrame).setVisible(true));
        passwordRecoveryButton.addActionListener(e -> new src.PasswordRecoveryGui(jFrame).setVisible(true));
        IPButton.addActionListener(e -> new src.IPreset(jFrame).setVisible(true));

        // 新增登录状态标签
        loginStatusLabel = new JLabel("", SwingConstants.CENTER);
        loginStatusLabel.setBounds(140, 250, 180, 20);
        jPanel.add(loginStatusLabel);

        // 新增取消自动登录按钮
        cancelAutoLoginButton = new JButton("取消自动登录");
        cancelAutoLoginButton.setBounds(160, 280, 120, 20);
        cancelAutoLoginButton.addActionListener(e -> cancelAutoLogin());
        cancelAutoLoginButton.setVisible(false);
        jPanel.add(cancelAutoLoginButton);

        loadLoginInfo();

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
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            String serverIP = src.ServerConfig.getServerIP();
            Socket socket = sslSocketFactory.createSocket(serverIP, 12345);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String hashedPassword = src.PasswordUtil.hashPassword(password);
            out.println("LOGIN:" + username + ":" + hashedPassword);

            String response = in.readLine();
            if (response != null && response.startsWith("SUCCESS")) {
                JOptionPane.showMessageDialog(jPanel, "登录成功！", "信息", JOptionPane.INFORMATION_MESSAGE);
                jFrame.dispose();
                new src.ChatGui(username, socket).setVisible(true);
                saveLoginInfo(username, password);
            } else {
                String errorMsg = (response == null) ? "连接超时" : response.split(":")[1];
                JOptionPane.showMessageDialog(jPanel, errorMsg, "错误", JOptionPane.ERROR_MESSAGE);
                socket.close();
            }
        } catch (IOException | NoSuchAlgorithmException | KeyManagementException ex) {
            JOptionPane.showMessageDialog(jPanel, "连接服务端失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void showRegistrationForm() {
        new src.RegisterGui(jFrame).setVisible(true);
    }

    private void saveLoginInfo(String username, String password) {
        if (rememberPasswordCheckBox.isSelected()) {
            Properties props = new Properties();
            props.setProperty("username", username);
            props.setProperty("password", password);
            props.setProperty("autoLogin", String.valueOf(autoLoginCheckBox.isSelected()));
            try (FileOutputStream fos = new FileOutputStream("login.properties")) {
                props.store(fos, "Login information");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            File file = new File("login.properties");
            if (file.exists()) {
                file.delete();
            }
        }
    }

    private void loadLoginInfo() {
        File file = new File("login.properties");
        if (file.exists()) {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
                String username = props.getProperty("username");
                String password = props.getProperty("password");
                boolean autoLogin = Boolean.parseBoolean(props.getProperty("autoLogin", "false"));

                jTextField.setText(username);
                jPasswordField.setText(password);
                rememberPasswordCheckBox.setSelected(true);
                autoLoginCheckBox.setSelected(autoLogin);

                if (autoLogin) {
                    startLoginTimer();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startLoginTimer() {
        loginStatusLabel.setText("正在登录中...");
        cancelAutoLoginButton.setVisible(true);

        int totalSeconds = 5;
        int delay = 1000; // 1秒
        final int[] remainingSeconds = {totalSeconds};

        loginTimer = new Timer(delay, e -> {
            if (remainingSeconds[0] > 0) {
                loginStatusLabel.setText("正在登录中... " + remainingSeconds[0] + " 秒");
                remainingSeconds[0]--;
            } else {
                loginTimer.stop();
                cancelAutoLoginButton.setVisible(false);
                onLoginButtonClicked(null);
            }
        });
        loginTimer.start();
    }

    private void cancelAutoLogin() {
        if (loginTimer != null) {
            loginTimer.stop();
        }
        loginStatusLabel.setText("");
        cancelAutoLoginButton.setVisible(false);
    }
}