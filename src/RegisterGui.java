package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.NoSuchAlgorithmException;

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


    private void onRegister() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
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

}
