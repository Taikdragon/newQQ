package src;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;

public class IPreset extends JDialog {

    private JTextField whatIP;

    public IPreset(JFrame parent) {
        super(parent, "服务器IP", true);
        setSize(350, 250);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 用户名输入
        JPanel IPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        IPanel.add(new JLabel("服务器IP:"));
        whatIP = new JTextField(15);
        IPanel.add(whatIP);
        panel.add(IPanel);

        JButton IPresetButton = new JButton("确定");
        panel.add(IPresetButton);


    }

}