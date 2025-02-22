package src;

import java.io.*;
import java.net.Socket;

public class ChatClient {
    private Socket socket;
    private PrintWriter out;
    private String username;

    // 使用现有Socket构造
    public ChatClient(Socket socket, String username) throws IOException {
        this.socket = socket;
        this.username = username;
        out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // 启动消息接收线程
        new Thread(new MessageReceiver(in)).start();
    }

    // 发送消息方法
    public void sendMessage(String message) {
        out.println(message);
    }

    // 消息接收线程
    private class MessageReceiver implements Runnable {
        private BufferedReader in;

        public MessageReceiver(BufferedReader in) {
            this.in = in;
        }

        @Override
        public void run() {
            try {
                String response;
                while ((response = in.readLine()) != null) {
                    src.ChatGui.appendMessage(response);
                }
            } catch (IOException e) {
                System.out.println("与服务器的连接已断开");
            }
        }
    }
}