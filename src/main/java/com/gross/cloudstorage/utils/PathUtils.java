package com.gross.cloudstorage.utils;

import com.gross.cloudstorage.exception.DirectoryPathValidationException;
import com.gross.cloudstorage.exception.UserPrefixException;

public class PathUtils {

    public static String addUserPrefix(long userId, String path) {
        if(!path.startsWith("user-" + userId + "-files/" + path))
        {return "user-" + userId + "-files/" + path;}
        else throw new UserPrefixException("Префикс пользователя уже существует");
    }

    public static String stripUserPrefix(String path,long userId) {
        String prefix = "user-" + userId + "-files/";
        if (path.startsWith(prefix)) {
            return path.substring(prefix.length());
        }
        throw new UserPrefixException("Ошибка при удалении префикса");
    }

    public static void validateDirectoryPath(String path) {
        if (path == null || path.isEmpty()) {
            throw new DirectoryPathValidationException("Путь директории не может быть пустым");
        }
        if (path.contains("..") || path.contains("//")) {
            throw new DirectoryPathValidationException("Недопустимые символы в пути");
        }
        if (!path.endsWith("/")) {
            throw new DirectoryPathValidationException("Путь директории должен заканчиваться на /");
        }
    }

    public static void validatePathToDeleteResource(String path) {
        if (path == null || path.isEmpty()) {
            throw new DirectoryPathValidationException("Путь к ресурсу не может быть пустым");
        }
        if (path.contains("..") || path.contains("//")) {
            throw new DirectoryPathValidationException("Недопустимые символы в пути");
        }
    }
}

