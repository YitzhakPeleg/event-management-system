package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for hashing passwords using SHA-256.
 *
 * Why SHA-256?
 *   Passwords are never stored as plain text. We hash them before saving
 *   to the database. On login, we hash the entered password and compare
 *   to the stored hash.
 *
 * Note: For production systems, bcrypt is preferred. SHA-256 is used
 * here for simplicity as this is a school project.
 */
public class HashUtil {

    // Private constructor — this is a utility class, no instances needed
    private HashUtil() {}

    /**
     * Hashes the given plain-text password using SHA-256.
     *
     * @param plainText the password to hash
     * @return the SHA-256 hash as a lowercase hex string (64 characters)
     * @throws RuntimeException if SHA-256 algorithm is not available (should never happen)
     */
    public static String hash(String plainText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plainText.getBytes());

            // Convert byte array to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found.", e);
        }
    }

    /**
     * Checks if a plain-text password matches a stored hash.
     *
     * @param plainText    the password entered by the user
     * @param storedHash   the hash stored in the database
     * @return true if they match
     */
    public static boolean matches(String plainText, String storedHash) {
        return hash(plainText).equals(storedHash);
    }
}
