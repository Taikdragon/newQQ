package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class IPreset extends JDialog {
    private JTextField ipField;

    public IPreset(JFrame parent) {
        super(parent, "服务器IP设置", true);
        setSize(350, 200);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // IP输入
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("服务器IP:"));
        ipField = new JTextField(15);
        ipField.setText(loadSavedIP()); // 加载已保存的IP
        inputPanel.add(ipField);
        panel.add(inputPanel);

        // 确定按钮
        JButton confirmButton = new JButton("确定");
        confirmButton.addActionListener(this::saveIP);
        panel.add(confirmButton);

        add(panel);
    }

    private void saveIP(ActionEvent e) {
        String newIP = ipField.getText().trim();
        if (!newIP.isEmpty()) {
            try (FileOutputStream out = new FileOutputStream("server_config.properties")) {
                Properties prop = new Properties();
                prop.setProperty("server.ip", newIP);
                prop.store(out, "Server Configuration");
                JOptionPane.showMessageDialog(this, "IP已保存！", "成功", JOptionPane.INFORMATION_MESSAGE);
                System.out.println("[控制台]：IP已保存！");
                dispose();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "保存失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "IP不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String loadSavedIP() {
        // 默认IP，如果配置文件不存在则返回此值
        //return "26.233.144.223";

        try (FileInputStream input = new FileInputStream("server_config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            return prop.getProperty("server.ip", "26.233.144.223"); // 默认IP
        } catch (Exception e) {
            return "26.233.144.223";
        }

    }
}