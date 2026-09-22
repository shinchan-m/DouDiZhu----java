package com.itheima.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * 负责从本地文件读取和保存 DeepSeek API Key。
 */
public final class ApiKeyStore {
    private static final Path KEY_FILE = AppPaths.apiKeyFile();

    private ApiKeyStore() {
    }

    public static String load() {
        try {
            migrateLegacyKey();
            if (Files.exists(KEY_FILE)) {
                String localKey = Files.readString(KEY_FILE, StandardCharsets.UTF_8).trim();
                if (!localKey.isEmpty()) {
                    return localKey;
                }
            }
        } catch (Exception e) {
            System.err.println("读取本地 DeepSeek Key 失败: " + e.getMessage());
        }

        String environmentKey = System.getenv("DEEPSEEK_API_KEY");
        return environmentKey == null ? "" : environmentKey.trim();
    }

    private static void migrateLegacyKey() throws Exception {
        Path legacyFile = Path.of("data", "deepseek.key");
        boolean homeFileEmpty = !Files.exists(KEY_FILE) || Files.size(KEY_FILE) == 0;
        if (homeFileEmpty && Files.exists(legacyFile) && Files.size(legacyFile) > 0) {
            Files.createDirectories(KEY_FILE.getParent());
            Files.copy(legacyFile, KEY_FILE,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void save(String apiKey) {
        String normalized = apiKey == null ? "" : apiKey.trim();
        if (normalized.isEmpty()) {
            clear();
            return;
        }
        try {
            Files.createDirectories(KEY_FILE.getParent());
            Files.writeString(KEY_FILE, normalized, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            throw new IllegalStateException("保存 DeepSeek API Key 失败", e);
        }
    }

    public static void clear() {
        try {
            Files.deleteIfExists(KEY_FILE);
        } catch (Exception e) {
            throw new IllegalStateException("清除 DeepSeek API Key 失败", e);
        }
    }
}
