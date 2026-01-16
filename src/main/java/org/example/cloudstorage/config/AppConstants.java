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

        public static final String PATH_REGEXP = "^[a-zA-Zа-яА-ЯёЁ0-9 /_.,!()\\-]+$";
        public static final Pattern PATH_PATTERN = Pattern.compile(PATH_REGEXP);

        public static final int MAX_PATH_LENGTH = 1024;

    }

    public static final class Validation {
        private Validation() {}

        // Лимиты
        public static final int USERNAME_MIN = 5;
        public static final int USERNAME_MAX = 20;
        public static final int PASSWORD_MIN = 5;
        public static final int PASSWORD_MAX = 20;

        // Регулярные выражения
        public static final String USERNAME_REGEXP = "^[a-zA-Z0-9]+[a-zA-Z_0-9]*[a-zA-Z0-9]+$";
        public static final String PASSWORD_REGEXP = "^[a-zA-Z0-9!@#$%^&*(),.?\":{}|<>[\\\\]/`~+=-_']*$";

        // Сообщения
        public static final String FIELD_REQUIRED = "Поле обязательно для заполнения";
        public static final String USERNAME_SIZE_MSG = "Логин должен быть от " + USERNAME_MIN + " до " + USERNAME_MAX + " символов";
        public static final String USERNAME_PATTERN_MSG = "Логин может содержать только латинские буквы, цифры и нижнее подчеркивание (не в начале и не в конце)";
        public static final String PASSWORD_SIZE_MSG = "Пароль должен быть от " + PASSWORD_MIN + " до " + PASSWORD_MAX + " символов";
        public static final String PASSWORD_PATTERN_MSG = "Пароль содержит недопустимые символы";
    }

    public static final class ExceptionMessages {

        private ExceptionMessages() {}

        public static final String MESSAGE_PATH_DELIMITER = ": ";

        // Дефолт ошибки и валидация
        public static final String INVALID_INPUT = "Переданы некорректные данные";
        public static final String INTERNAL_SERVER_ERROR = "Внутренняя ошибка сервиса";

        // Лимиты
        public static final String MAX_FILE_COUNT_EXCEEDED = "Превышено максимальное количество файлов";
        public static final String MAX_STORAGE_SIZE_EXCEEDED = "Превышен максимальный размер хранилища";

        // Ошибки хранилища
        public static final String RESOURCE_NOT_FOUND = "Ресурс не найден";
        public static final String CONFLICT = "Ресурс с таким именем уже существует";
        public static final String ACCESS_DENIED = "Доступ запрещен";
        public static final String STORAGE_ERROR = "Внутренняя ошибка хранилища";
        public static final String STORAGE_UNAVAILABLE = "Сервис хранилища временно недоступен";

        public static final String PATH_TOO_LONG = "Путь не может быть длиннее " + Storage.MAX_PATH_LENGTH + " символов";
        public static final String PATH_INVALID_CHARACTERS = "Путь содержит недопустимые символы";
        public static final String PATH_CONTAINS_DOTS = "Путь не может содержать '..'";
        public static final String PATH_MUST_BE_DIRECTORY = "Путь к директории должен заканчиваться на '/'";
        public static final String MOVE_TYPE_MISMATCH = "Нельзя менять тип ресурса (файл/папка) при перемещении";
        public static final String MOVE_INTO_ITSELF = "Нельзя переместить папку в саму себя или свою подпапку";
        public static final String FOLDER_PARENT_MISSING = "Родительская папка не существует. Вручную нельзя создавать вложенные папки";

        // Ошибки пользователей
        public static final String USER_NOT_FOUND = "Пользователь не найден";
        public static final String USER_ALREADY_EXISTS = "Пользователь с таким именем уже существует";
        public static final String UNAUTHORIZED = "Для доступа к ресурсу требуется авторизация";
        public static final String BAD_CREDENTIALS = "Неверное имя пользователя или пароль";
    }
}
