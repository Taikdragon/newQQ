package src;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.io.*;
import java.net.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;

public class RegisterGui extends JDialog{

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton registerButton;
    private JButton cancelButton;
    private JPanel panel;

    public RegisterGui(JFrame parent) {
        super(parent, "注册账户", true); // 设置为模态对话框
        setSize(350, 300);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parent); // 相对于父窗口居中显示

        // 创建面板和布局
        panel = new JPanel(new GridLayout(5, 2, 5, 5));
        setupComponents();

        // 添加按钮
        registerButton = new JButton("注册");
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onRegister();
            }
        });
        panel.add(registerButton);

        cancelButton = new JButton("取消");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // 关闭窗口
            }
        });
        panel.add(cancelButton);

        // 将面板添加到对话框
        add(panel);
    }

    private void setupComponents() {
        // 添加用户名输入框
        panel.add(new JLabel("用户名:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        // 添加密码输入框
        panel.add(new JLabel("密码:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        // 可以继续添加其他注册所需字段...
    }

/*
    private void onRegister() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {

            System.out.println("Register is empty");

            JOptionPane.showMessageDialog(this, "用户名和密码不能为空！", "注册错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // 哈希密码
            String hashedPassword = src.UserStorage.hashPassword(password);
            // 保存用户

            System.out.println("Register Username Hash: " + username);
            System.out.println("Register Password Hash: " + hashedPassword);

            if (src.UserStorage.addUser(username, hashedPassword)) {
                JOptionPane.showMessageDialog(this, "注册成功，欢迎 " + username + "！", "注册成功", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "用户名已存在或保存失败！", "注册错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NoSuchAlgorithmException e) {
            JOptionPane.showMessageDialog(this, "密码加密失败！", "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

 */

    private void onRegister() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名和密码不能为空！", "注册错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        /*
        try {
            // 连接服务端
            Socket socket = new Socket("192.168.0.103", 12345);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 发送注册命令：REGISTER:用户名:密码哈希
            String hashedPassword = src.UserStorage.hashPassword(password);
            out.println("REGISTER:" + username + ":" + hashedPassword);

            // 接收服务端响应
            String response = in.readLine();
            if (response == null) {
                JOptionPane.showMessageDialog(this, "服务端无响应", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String[] parts = response.split(":", 2); // 分割为最多2部分
            if (parts.length < 2) {
                JOptionPane.showMessageDialog(this, "响应格式错误", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (parts[0].equals("SUCCESS")) {
                JOptionPane.showMessageDialog(this, "注册成功！", "注册成功", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, parts[1], "注册错误", JOptionPane.ERROR_MESSAGE);
            }

            socket.close();
        } catch (IOException | NoSuchAlgorithmException e) {
            JOptionPane.showMessageDialog(this, "连接服务端失败！", "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

         */

        try {
            // 配置 SSLContext 信任所有证书
            SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };
            sslContext.init(null, trustAllCerts, new SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            // 使用 SSLSocket 连接

            String serverIP = src.ServerConfig.getServerIP();
            Socket socket = sslSocketFactory.createSocket(serverIP, 12345);

            //Socket socket = sslSocketFactory.createSocket("26.233.144.223", 12345);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 发送注册命令
            String hashedPassword = src.UserStorage.hashPassword(password);
            out.println("REGISTER:" + username + ":" + hashedPassword);

            // 处理响应
            String response = in.readLine();
            if (response == null) {
                JOptionPane.showMessageDialog(this, "服务端无响应", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String[] parts = response.split(":", 2);
            if (parts.length < 2) {
                JOptionPane.showMessageDialog(this, "响应格式错误", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (parts[0].equals("SUCCESS")) {
                JOptionPane.showMessageDialog(this, "注册成功！", "注册成功", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, parts[1], "注册错误", JOptionPane.ERROR_MESSAGE);
            }

            socket.close();
        } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
            JOptionPane.showMessageDialog(this, "连接服务端失败！", "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

    }

}
