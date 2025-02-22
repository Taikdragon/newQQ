package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;

public class ChatClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public ChatClient(String serverAddress, int serverPort, String username) throws IOException {
        this.username = username;
        socket = new Socket(serverAddress, serverPort);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // 发送用户名给服务器
        out.println(username);

        // 启动消息接收线程
        new Thread(new MessageReceiver()).start();
    }

    // 发送消息方法
    public void sendMessage(String message) {
        if (out != null) {
            System.out.println("[DEBUG] 发送消息: " + message); // 调试日志
            out.println(message);
        } else {
            System.out.println("[ERROR] 输出流未初始化");
        }
    }

    // 消息接收线程
    private class MessageReceiver implements Runnable {
        @Override
        public void run() {
            try {
                String response;
                while ((response = in.readLine()) != null) { // 必须持续监听
                    src.ChatGui.appendMessage(response); // 必须调用此方法
                }
            } catch (IOException e) {
                System.out.println("与服务器的连接已断开");
            }
        }
    }
}