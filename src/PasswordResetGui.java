package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

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

    private void showError(String msg) {
        statusLabel.setText(msg);
        statusLabel.setForeground(Color.RED);
    }
}