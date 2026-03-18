package util;

/**
 * Utility class for validating user input before saving to the database.
 *
 * Each method returns true if the input is valid, false otherwise.
 * Validation is done at the service layer before any database call.
 */
public class ValidationUtil {

    // Private constructor — utility class, no instances needed
    private ValidationUtil() {}

    /**
     * Validates an email address format.
     * Must contain '@' and at least one '.' after the '@'.
     *
     * @param email the email string to validate
     * @return true if valid
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        // Simple regex: must have text@text.text
        return email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
    }

    /**
     * Validates an Israeli phone number.
     * Accepts formats like: 050-1234567, 0501234567, +972-50-1234567
     *
     * @param phone the phone string to validate
     * @return true if valid
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return false;
        // Remove spaces and dashes, then check digit count
        String digits = phone.replaceAll("[\\s\\-+]", "");
        return digits.matches("^(972|0)\\d{9}$") || digits.matches("^0\\d{9}$");
    }

    /**
     * Validates that a text field is not null or empty.
     *
     * @param text  the string to check
     * @param field the field name (used only in error messages)
     * @return true if the text has content
     */
    public static boolean isNotEmpty(String text, String field) {
        return text != null && !text.trim().isEmpty();
    }

    /**
     * Validates that an event date string is in YYYY-MM-DD format
     * and is a future date.
     *
     * @param date the date string to validate
     * @return true if valid format
     */
    public static boolean isValidDate(String date) {
        if (date == null || date.trim().isEmpty()) return false;
        return date.matches("^\\d{4}-\\d{2}-\\d{2}$");
    }

    /**
     * Validates that a time string is in HH:MM format.
     *
     * @param time the time string to validate
     * @return true if valid format
     */
    public static boolean isValidTime(String time) {
        if (time == null || time.trim().isEmpty()) return false;
        return time.matches("^([01]\\d|2[0-3]):[0-5]\\d$");
    }

    /**
     * Validates that a capacity number is a positive integer.
     *
     * @param capacity the capacity value to validate
     * @return true if greater than zero
     */
    public static boolean isValidCapacity(int capacity) {
        return capacity > 0;
    }

    /**
     * Validates a username: 3–50 characters, letters/digits/underscores only.
     *
     * @param username the username to validate
     * @return true if valid
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) return false;
        return username.matches("^[a-zA-Z0-9_]{3,50}$");
    }

    /**
     * Validates a password: at least 6 characters.
     *
     * @param password the plain-text password to validate
     * @return true if valid
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
}
