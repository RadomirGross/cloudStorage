package com.gross.cloudstorage.utils;

import com.gross.cloudstorage.exception.PathValidationException;
import com.gross.cloudstorage.exception.UserPrefixException;

public class PathUtils {

    public static String addUserPrefix(long userId, String path) {
        if (path == null) {
            path = "";
        }

        if (!path.startsWith("user-" + userId + "-files/" + path)) {
            return "user-" + userId + "-files/" + path;
        } else throw new UserPrefixException("Префикс пользователя уже существует");
    }

    public static String stripUserPrefix(long userId, String path) {
        String prefix = "user-" + userId + "-files/";
        if (path.startsWith(prefix)) {
            return path.substring(prefix.length());
        }
        throw new UserPrefixException("Ошибка при удалении префикса. Префикс отсутствует.");
    }

    public static void validatePath(String fullPath, boolean isDirectory) {
        if (fullPath == null || fullPath.isEmpty()) {
            throw new PathValidationException("Путь " + (isDirectory ? "директории" : "файла") + " не может быть пустым");
        }
        String path = PathUtils.extractPath(fullPath);
        if (path.contains("..") || path.contains("//") || path.contains("\\")) {
            throw new PathValidationException("Недопустимые символы в пути");
        }
        if (isDirectory && !fullPath.endsWith("/")) {
            throw new PathValidationException("Путь директории должен заканчиваться на /");
        }

        if (!isDirectory && fullPath.endsWith("/")) {
            throw new PathValidationException("Путь файла не должен заканчиваться на /");
        }
    }

    public static void validatePathToDeleteResource(String path) {

        if (path == null || path.isEmpty()) {
            throw new PathValidationException("Путь к ресурсу не может быть пустым");
        }
        if (path.contains("..") || path.contains("//")) {
            throw new PathValidationException("Недопустимые символы в пути");
        }
    }

    public static void validateSearchRequest(String query) {

        if (query == null || query.trim().isEmpty()) {
            throw new PathValidationException("Поисковой запрос не может быть пустым");
        }
        if (!query.matches("^[a-zA-Z0-9а-яА-Я._\\-/ ]+$")) {
            throw new PathValidationException("Поисковой запрос содержит недопустимые символы");
        }
    }

    public static String extractName(String path) {
        String[] parts = path.split("/");
        boolean isRootDirectory = parts.length == 1;
        if (isRootDirectory) return "";

        if (path.endsWith("/")) {
            return parts[parts.length - 1] + "/";
        } else return parts[parts.length - 1];
    }

    public static String extractPath(String path) {
        String name = extractName(path);
        return path.substring(0, path.length() - name.length());
    }
}

