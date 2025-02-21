package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PasswordRecoveryGui extends JDialog {
    private JTextField usernameField;
    private JLabel resultLabel;

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
            resultLabel.setText("用户名不能为空！");
            resultLabel.setForeground(Color.RED);
            return;
        }

        Properties prop = new Properties();
        try (FileInputStream input = new FileInputStream(src.UserStorage.USER_DATA_FILE)) {
            prop.load(input);
            String storedHash = prop.getProperty(username);

            System.out.println("Search Hash: " + storedHash);

            if (storedHash != null) {
                resultLabel.setText("密码哈希值: " + storedHash);
                resultLabel.setForeground(Color.BLUE);
            } else {
                resultLabel.setText("此用户未注册！");
                resultLabel.setForeground(Color.RED);
            }
        } catch (IOException ex) {
            resultLabel.setText("数据读取失败！");
            resultLabel.setForeground(Color.RED);
            ex.printStackTrace();
        }
    }
}