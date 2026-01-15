package org.example.cloudstorage.config;

import java.util.regex.Pattern;

public final class AppConstants {

    private AppConstants() {}

    public static final class Storage {

        private Storage() {}

        public static final String USER_ROOT_TEMPLATE = "user-%d-files/";

        public static final Pattern USER_PREFIX_PATTERN = Pattern.compile(
                "^" + USER_ROOT_TEMPLATE.replace("%d", "\\d+") + "(.*)"
        );
    }

    public static final class ExceptionMessages {

        private ExceptionMessages() {}

        public static final String MESSAGE_PATH_DELIMITER = ": ";

        // Дефолт ошибки и валидация
        public static final String INVALID_INPUT = "Переданы некорректные данные";

        // Ошибки хранилища
        public static final String RESOURCE_NOT_FOUND = "Ресурс не найден";
        public static final String CONFLICT = "Ресурс с таким именем уже существует";
        public static final String ACCESS_DENIED = "Доступ запрещен";
        public static final String STORAGE_ERROR = "Внутренняя ошибка хранилища";
        public static final String STORAGE_UNAVAILABLE = "Сервис хранилища временно недоступен";

        // Ошибки пользователей
        public static final String USER_NOT_FOUND = "Пользователь не найден";
        public static final String USER_ALREADY_EXISTS = "Пользователь с таким именем уже существует";
        public static final String UNAUTHORIZED = "Для доступа к ресурсу требуется авторизация";
        public static final String BAD_CREDENTIALS = "Неверное имя пользователя или пароль";
    }
}
