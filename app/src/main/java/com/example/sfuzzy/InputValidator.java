package com.example.sfuzzy;

import android.text.TextUtils;

public class InputValidator {

    // Статический метод для проверки
    public static ValidationResult validateRegistrationInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            return new ValidationResult(false, "Enter email");
        }

        if (TextUtils.isEmpty(password)) {
            return new ValidationResult(false, "Enter password");
        }

        if (!isValidEmail(email)) {
            return new ValidationResult(false, "Invalid email format");
        }

        if (password.length() < 6) {
            return new ValidationResult(false, "Password must be at least 6 characters");
        }

        return new ValidationResult(true, "");
    }

    private static boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Статический внутренний класс
    public static class ValidationResult {
        private final boolean isValid;
        private final String errorMessage;

        public ValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return isValid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}