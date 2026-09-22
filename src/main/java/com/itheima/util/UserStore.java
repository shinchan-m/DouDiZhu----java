package com.itheima.util;

import com.itheima.domain.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * 负责把注册账号加载到内存，并同步保存到 data/users.txt。
 */
public class UserStore {
    private final Path userFile;
    private final List<User> users = new ArrayList<>();

    public UserStore() {
        this(AppPaths.userFile());
    }

    public UserStore(Path userFile) {
        this.userFile = userFile;
        load();
    }

    private void load() {
        try {
            Files.createDirectories(userFile.getParent());
            migrateLegacyFile();
            if (!Files.exists(userFile)) {
                Files.createFile(userFile);
                return;
            }
            for (String line : Files.readAllLines(userFile, StandardCharsets.UTF_8)) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split("\\t", -1);
                if (parts.length == 2) {
                    users.add(new User(parts[0], parts[1]));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("读取用户信息失败", e);
        }
    }

    private void migrateLegacyFile() throws IOException {
        Path legacyFile = Path.of("data", "users.txt");
        boolean homeFileEmpty = !Files.exists(userFile) || Files.size(userFile) == 0;
        if (homeFileEmpty && Files.exists(legacyFile) && Files.size(legacyFile) > 0) {
            Files.copy(legacyFile, userFile,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public boolean usernameExists(String username) {
        return users.stream().anyMatch(user -> user.username().equals(username));
    }

    public boolean authenticate(String username, String password) {
        return users.stream().anyMatch(user ->
                user.username().equals(username) && user.password().equals(password));
    }

    public void register(String username, String password) {
        if (usernameExists(username)) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (username.contains("\t") || username.contains("\n")
                || password.contains("\t") || password.contains("\n")) {
            throw new IllegalArgumentException("用户名或密码不能包含制表符或换行");
        }
        try {
            Files.createDirectories(userFile.getParent());
            Files.writeString(userFile, username + "\t" + password + System.lineSeparator(),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            users.add(new User(username, password));
        } catch (IOException e) {
            throw new IllegalStateException("保存用户信息失败", e);
        }
    }
}
