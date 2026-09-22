package com.itheima.util;

import java.nio.file.Path;

/**
 * 应用运行数据统一放在用户目录，避免依赖项目所在磁盘位置。
 */
public final class AppPaths {
    private static final Path DATA_DIR =
            Path.of(System.getProperty("user.home"), ".doudizhu");

    private AppPaths() {
    }

    public static Path dataDir() {
        return DATA_DIR;
    }

    public static Path userFile() {
        return DATA_DIR.resolve("users.txt");
    }

    public static Path apiKeyFile() {
        return DATA_DIR.resolve("deepseek.key");
    }
}
