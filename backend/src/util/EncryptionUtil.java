package util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Utility class for AES encryption and decryption.
 *
 * Why encryption?
 *   Participant email and phone are sensitive personal data.
 *   We encrypt them before saving to the database so that even if
 *   someone accesses the database directly, they cannot read the data.
 *
 * How it works:
 *   - AES (Advanced Encryption Standard) with a 16-character secret key.
 *   - encrypt(): converts plain text → encrypted bytes → Base64 string (safe for DB storage).
 *   - decrypt(): converts Base64 string → encrypted bytes → plain text.
 */
public class EncryptionUtil {

    // Secret key — must be exactly 16 characters (128-bit AES key)
    // In a real system this would be stored in a config file, not in code.
    private static final String SECRET_KEY = "EventMgmt2026Key";

    private static final String ALGORITHM = "AES";

    // Private constructor — utility class, no instances needed
    private EncryptionUtil() {}

    /**
     * Encrypts plain text using AES and returns a Base64-encoded string.
     *
     * @param plainText the text to encrypt (e.g. "yossi@example.com")
     * @return Base64-encoded encrypted string, safe to store in the database
     * @throws RuntimeException if encryption fails
     */
    public static String encrypt(String plainText) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypts a Base64-encoded AES-encrypted string back to plain text.
     *
     * @param encryptedText the Base64-encoded encrypted string from the database
     * @return the original plain text (e.g. "yossi@example.com")
     * @throws RuntimeException if decryption fails
     */
    public static String decrypt(String encryptedText) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed: " + e.getMessage(), e);
        }
    }
}
