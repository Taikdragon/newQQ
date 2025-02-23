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
        out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(15000);
                    out.println("HEARTBEAT:" + username + ":");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(new MessageReceiver(in)).start();
    }

    public void sendMessage(String message) {
        out.println(message);
    }

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
                    if (response.startsWith("USERS:")) {
                        String[] users = response.split(":")[1].split(",");
                        SwingUtilities.invokeLater(() -> src.ChatGui.updateUserList(users));
                    } else {
                        src.ChatGui.appendMessage(response);
                    }
                }
            } catch (IOException e) {
                System.out.println("与服务器的连接已断开");
            }
        }
    }
}