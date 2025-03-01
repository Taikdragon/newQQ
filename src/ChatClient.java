package src;

import java.io.*;
import java.net.Socket;
import javax.swing.*;

public class ChatClient {
    private Socket socket;
    private PrintWriter out;
    private String username;

    public ChatClient(Socket socket, String username) throws IOException {
        this.socket = socket;
        this.username = username;
        this.out = new PrintWriter(socket.getOutputStream(), true);

        // 启动消息接收线程
        new Thread(new MessageReceiver(socket)).start();
    }

    /**
     * 发送消息到服务端
     */
    public void sendMessage(String message) {
        if (message.startsWith("/who")) {
            // 修正协议格式：LIST_USERS:用户名
            out.println("LIST_USERS:" + username);
        } else if (message.startsWith("/history ")) {
            String count = message.substring(9);
            // 修正协议格式：HISTORY:用户名:条数
            out.println("HISTORY:" + username + ":" + count);
        } else {
            out.println(message);
        }
    }

    /**
     * 消息接收线程（持续监听服务端消息）
     */
    /*
    private class MessageReceiver implements Runnable {
        private BufferedReader in;

        public MessageReceiver(Socket socket) throws IOException {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        @Override
        public void run() {
            try {
                String response;
                while ((response = in.readLine()) != null) {  // 持续监听消息
                    if (response.startsWith("USERS:")) {
                        // 处理用户列表更新
                        String[] users = response.split(":")[1].split(",");
                        SwingUtilities.invokeLater(() -> src.ChatGui.updateUserList(users));
                    } else {
                        // 处理普通消息
                        SwingUtilities.invokeLater(() -> src.ChatGui.appendMessage(response));
                    }
                }
            } catch (IOException e) {
                System.out.println("与服务器的连接已断开");
            } finally {
                try {
                    socket.close();  // 确保关闭连接
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

     */

    private class MessageReceiver implements Runnable {
        private BufferedReader in;

        public MessageReceiver(Socket socket) throws IOException {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        @Override
        public void run() {
            try {
                String response;
                while ((response = in.readLine()) != null) {
                    if (response.startsWith("USERS:")) {
                        String[] users = response.split(":")[1].split(",");
                        SwingUtilities.invokeLater(() -> src.ChatGui.updateUserList(users));
                    } else {
                        final String finalResponse = response;
                        SwingUtilities.invokeLater(() -> src.ChatGui.appendMessage(finalResponse));
                    }
                }
            } catch (IOException e) {
                System.out.println("与服务器的连接已断开");
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

}