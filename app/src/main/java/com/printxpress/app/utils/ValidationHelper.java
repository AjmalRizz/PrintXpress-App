package com.printxpress.app.utils;

import android.util.Patterns;

public class ValidationHelper {

    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    public static boolean isValidSriLankanPhone(String phone) {
        if (phone == null) return false;
        String cleanPhone = phone.trim();
        return cleanPhone.matches("^07[0-9]{8}$");
    }

    public static boolean isPasswordStrong(String password) {
        return password != null && password.trim().length() >= 6;
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isValidFileType(String filename) {
        if (filename == null) return false;
        String lowercase = filename.toLowerCase();
        return lowercase.endsWith(".jpg") || lowercase.endsWith(".jpeg") || lowercase.endsWith(".png") || lowercase.endsWith(".pdf");
    }
}
