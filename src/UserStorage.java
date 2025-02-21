package src;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Properties;

public class UserStorage {
    public static final String USER_DATA_FILE = "user_data.properties";
    //private static final String USER_DATA_FILE = "user_data.properties";
    // 将USER_DATA_FILE改为public使其可以访问

    static {
        try {
            File file = new File(USER_DATA_FILE);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean validateUser(String username, String passwordHash) {
        try (InputStream input = new FileInputStream(USER_DATA_FILE)) {
            Properties prop = new Properties();
            prop.load(input);
            String storedHash = prop.getProperty(username);
            System.out.println("Stored Hash: " + storedHash);
            System.out.println("Input Hash: " + passwordHash);
            return storedHash != null && storedHash.equals(passwordHash);
        } catch (IOException | NullPointerException e) {
            return false;
        }
    }

    public static boolean addUser(String username, String passwordHash) {
        Properties prop = new Properties();
        // 先读取现有数据
        try (InputStream input = new FileInputStream(USER_DATA_FILE)) {
            prop.load(input);
        } catch (IOException e) {
            // 文件不存在时忽略，继续新建
        }

        // 检查用户名是否存在
        if (prop.containsKey(username)) {
            return false;
        }

        // 添加新用户
        prop.setProperty(username, passwordHash);

        // 写入所有数据（覆盖模式）
        try (OutputStream output = new FileOutputStream(USER_DATA_FILE)) {
            prop.store(output, "User registration data");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(password.getBytes(StandardCharsets.UTF_8));
        byte[] bytes = md.digest();
        return Base64.getEncoder().encodeToString(bytes);
    }
}