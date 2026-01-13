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
}
