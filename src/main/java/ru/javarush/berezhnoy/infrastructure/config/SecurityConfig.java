package ru.javarush.berezhnoy.infrastructure.config;

import java.util.List;

public class SecurityConfig {
    private static final List<String> PROTECTED_PATHS;
    private static final List<String> BLOCKED_EXTENSIONS;

    static {
        PROTECTED_PATHS = ApplicationConfig.getListProperty(
                "caesar.security.protected.paths", "\\|");
        BLOCKED_EXTENSIONS = ApplicationConfig.getListProperty(
                "caesar.security.blocked.extensions", "\\|");
    }

    public static boolean isPathProtected(String path) {
        String normalizedPath = path.toLowerCase();
        return PROTECTED_PATHS.stream()
                .anyMatch(protectedPath -> normalizedPath.contains(protectedPath.toLowerCase()));
    }

    public static boolean isExtensionBlocked(String filename) {
        if (filename == null) return false;
        String lowerFilename = filename.toLowerCase();
        return BLOCKED_EXTENSIONS.stream()
                .anyMatch(ext -> lowerFilename.endsWith(ext));
    }
}
