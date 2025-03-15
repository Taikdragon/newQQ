package src;

import java.io.FileInputStream;
import java.util.Properties;

public class ServerConfig {
    public static String getServerIP() {
        try (FileInputStream input = new FileInputStream("server_config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            return prop.getProperty("server.ip", "26.233.144.223"); // 设置默认IP
        } catch (Exception e) {
            return "26.233.144.223";
        }
    }
}