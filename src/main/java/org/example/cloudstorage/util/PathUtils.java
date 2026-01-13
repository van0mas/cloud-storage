package org.example.cloudstorage.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PathUtils {

    private PathUtils() {}

    public static String extractRelativePath(Pattern pattern, String fullPath) {
        Matcher matcher = pattern.matcher(fullPath);
        return matcher.matches() ? matcher.group(1) : fullPath;
    }

    public static String extractParentPath(String path) {
        if (isRoot(path)) return "";
        String cleanPath = trimTrailingSlash(path);
        int lastSlashIdx = cleanPath.lastIndexOf('/');
        if (lastSlashIdx == -1) return "";
        return cleanPath.substring(0, lastSlashIdx + 1);
    }

    public static String extractName(String path) {
        if (isRoot(path)) return "";
        String cleanPath = trimTrailingSlash(path);
        int lastSlashIdx = cleanPath.lastIndexOf('/');
        return (lastSlashIdx == -1) ? cleanPath : cleanPath.substring(lastSlashIdx + 1);
    }

    public static String trimTrailingSlash(String path) {
        return (path != null && path.endsWith("/") && path.length() > 1)
                ? path.substring(0, path.length() - 1)
                : path;
    }

    public static List<String> getParentSteps(String path) {
        List<String> steps = new ArrayList<>();
        String[] parts = path.split("/");
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < parts.length - 1; i++) {
            if (parts[i].isEmpty()) continue;
            current.append(parts[i]).append("/");
            steps.add(current.toString());
        }
        return steps;
    }

    public static String normalize(String path) {
        if (isRoot(path)) return "";

        return path.trim()
                .replace("\\", "/")
                .replaceAll("/{2,}", "/")
                .replaceAll("^/+", "");
    }

    public static String getDownloadName(String path) {
        String name = extractName(path);
        if (name.isEmpty()) name = "root";

        return path.endsWith("/") ? name + ".zip" : name;
    }

    public static boolean isRoot(String path) {
        return path == null || path.isEmpty() || "/".equals(path);
    }
}
