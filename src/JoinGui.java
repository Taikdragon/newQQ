package src;

import javax.net.ssl.SSLSocketFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.NoSuchAlgorithmException;
import java.io.*;
import java.net.*;
import java.util.*;

public class JoinGui {

    private JFrame jFrame;
    private JPanel jPanel;
    private JLabel head;
    private JLabel QQhao;
    private JLabel mima;
    private JCheckBox jCheckBox;
    private JCheckBox jChneweckBox1;
    private JButton passworld;
    private JButton name;
    private JTextField jTextField;
    private JPasswordField jPasswordField;

    public static void main(String[] args) {
        // 确保GUI的创建和更新在事件分派线程中执行
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new JoinGui().createAndShowGUI();
            }
        });
    }

    public void createAndShowGUI() {
        // 初始化jFrame
        jFrame = new JFrame();
        jFrame.setTitle("QQ登录界面");
        jFrame.setSize(450, 400);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setLocationRelativeTo(null);

        // 初始化head标签并设置图片new
        /*
        head = new JLabel(new ImageIcon(JoinGui.class.getResource("/src/picture/head.png")));
        jFrame.add(head, BorderLayout.NORTH);

         */

        // 初始化jPanel
        jPanel = new JPanel();
        jPanel.setLayout(null);

        // 初始化登录按钮并添加事件监听器
        /*
        JButton login = new JButton(new ImageIcon(JoinGui.class.getResource("/src/picture/LOGIN.png")));
        login.setBounds(177, 200, 80, 37);
        login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onLoginButtonClicked();
            }
        });
        jPanel.add(login);

         */

        JButton login = new JButton("登录");
        login.setBounds(177, 200, 80, 37);
        login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onLoginButtonClicked();
            }
        });
        jPanel.add(login);

        // 初始化文本框和密码框
        jTextField = new JTextField();
        jTextField.setBounds(140, 10, 180, 30);
        jPanel.add(jTextField);

        jPasswordField = new JPasswordField();
        jPasswordField.setBounds(140, 50, 180, 30);
        jPanel.add(jPasswordField);

        // 初始化其他标签和复选框
        QQhao = new JLabel("QQ号:");
        QQhao.setBounds(100, 10, 50, 20);
        jPanel.add(QQhao);

        mima = new JLabel("密码:");
        mima.setBounds(105, 50, 50, 20);
        jPanel.add(mima);

        jCheckBox = new JCheckBox("记住密码");
        jCheckBox.setBounds(180, 80, 80, 20);
        jPanel.add(jCheckBox);

        jCheckBox = new JCheckBox("自动登录");
        jCheckBox.setBounds(180, 105, 80, 20);
        jPanel.add(jCheckBox);

        passworld = new JButton("找回密码");
        passworld.setBounds(330, 15, 85, 20);
        jPanel.add(passworld);

        name = new JButton("注册账户");
        name.setBounds(330, 55, 85, 20);
        jPanel.add(name);

        name.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRegistrationForm(); // 方法名修改为 showRegistrationForm
            }
        });

        name = new JButton("重置账户");
        name.setBounds(330, 95, 85, 20);
        jPanel.add(name);

        name.addActionListener(e -> {
            // 弹出密码重置窗口
            new src.PasswordResetGui(jFrame).setVisible(true);
        });

        jFrame.add(jPanel);
        jFrame.setVisible(true);

        passworld.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new src.PasswordRecoveryGui(jFrame).setVisible(true);
            }
        });


    }

    /*
    private void onLoginButtonClicked() {
        String qqNumber = jTextField.getText();
        char[] passwordChars = jPasswordField.getPassword();
        String password = new String(passwordChars);

        // 添加输入非空检查
        if (qqNumber.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(jPanel, "QQ号或密码不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
            java.util.Arrays.fill(passwordChars, '0'); // 清除密码
            return;
        }

        try {
            String hashedPassword = src.UserStorage.hashPassword(password);
            System.out.println("Login Hash: " + hashedPassword); // 调试输出
            boolean isValid = src.UserStorage.validateUser(qqNumber, hashedPassword);
            System.out.println("Validation Result: " + isValid); // 调试输出

            // 修改登录成功部分代码
            if (isValid) {
                JOptionPane.showMessageDialog(jPanel, "登录成功！", "信息", JOptionPane.INFORMATION_MESSAGE);
                jFrame.dispose(); // 关闭登录窗口
                new src.ChatGui(qqNumber).setVisible(true); // 打开聊天窗口
            } else {
                JOptionPane.showMessageDialog(jPanel, "QQ号或密码错误！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NoSuchAlgorithmException e) {
            JOptionPane.showMessageDialog(jPanel, "密码验证失败！", "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            java.util.Arrays.fill(passwordChars, '0'); // 清除密码
        }
    }

     */

    private void onLoginButtonClicked() {
        String username = jTextField.getText();
        String password = new String(jPasswordField.getPassword());

        try {
            // 建立连接（不关闭）
            //Socket socket = new Socket("192.168.0.103", 12345);

            // 替换原有Socket连接代码
            SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            Socket socket = sslSocketFactory.createSocket("192.168.0.103", 12345);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 发送登录命令
            String hashedPassword = src.UserStorage.hashPassword(password);
            out.println("LOGIN:" + username + ":" + hashedPassword);

            // 接收响应
            String response = in.readLine();
            if (response.startsWith("SUCCESS")) {
                JOptionPane.showMessageDialog(jPanel, "登录成功！", "信息", JOptionPane.INFORMATION_MESSAGE);
                jFrame.dispose();
                // 传递Socket到聊天界面
                new src.ChatGui(username, socket).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(jPanel, response.split(":")[1], "错误", JOptionPane.ERROR_MESSAGE);
                socket.close(); // 登录失败时关闭
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            JOptionPane.showMessageDialog(jPanel, "连接服务端失败！", "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void showRegistrationForm() {
        // 显示注册界面
        new src.RegisterGui(this.jFrame).setVisible(true);
    }

}