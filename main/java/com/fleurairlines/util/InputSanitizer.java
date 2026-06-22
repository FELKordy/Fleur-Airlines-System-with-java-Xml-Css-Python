package com.fleurairlines.util;

import java.util.regex.Pattern;

public class InputSanitizer {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+]?[0-9]{7,15}$"
    );
    private static final Pattern PASSPORT_PATTERN = Pattern.compile(
        "^[A-Z0-9]{6,9}$"
    );
    private static final Pattern NAME_PATTERN = Pattern.compile(
        "^[a-zA-Z\\s'-]{2,50}$"
    );

    public static String sanitizeText(String input) {
        if (input == null) {
            return "";
        }
        return input.trim()
            .replaceAll("[<>\"'%;()&+]", "")
            .replaceAll("\\s+", " ");
    }

    public static String sanitizeForXml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;");
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches() && email.length() <= 100;
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidPassport(String passport) {
        if (passport == null || passport.isEmpty()) {
            return false;
        }
        return PASSPORT_PATTERN.matcher(passport).matches();
    }

    public static boolean isValidName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        return NAME_PATTERN.matcher(name).matches();
    }
}
