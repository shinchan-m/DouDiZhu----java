package com.itheima.util;

import javax.swing.ImageIcon;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ResourceUtil {
    private static final String IMAGE_ROOT = "farmerandlord/image/";

    private ResourceUtil() {
    }

    public static ImageIcon loadImage(String relativePath) {
        String normalized = relativePath.replace('\\', '/');
        URL resource = ResourceUtil.class.getClassLoader()
                .getResource(IMAGE_ROOT + normalized);
        if (resource != null) {
            return new ImageIcon(resource);
        }

        Path developmentPath = Path.of("src", "main", "resources", IMAGE_ROOT, normalized);
        if (Files.exists(developmentPath)) {
            return new ImageIcon(developmentPath.toString());
        }

        Path legacyPath = Path.of("farmerandlord", "image", normalized);
        if (Files.exists(legacyPath)) {
            return new ImageIcon(legacyPath.toString());
        }
        throw new IllegalStateException("找不到图片资源: " + IMAGE_ROOT + normalized);
    }

    public static ImageIcon loadPokerIcon(String fileName) {
        return loadImage("poker/" + fileName);
    }
}
