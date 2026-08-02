package getticket.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Password hashing using PBKDF2WithHmacSHA256 — a slow, salted,
 * iterated hash. Slow is the point: it makes brute-forcing a stolen
 * password list expensive. Built entirely on the JDK, no external
 * dependency required.
 *
 * Stored format: "iterations:base64(salt):base64(hash)"
 * e.g. "65536:k3F2p9...:aZ81mQ..." — fits comfortably in Password VARCHAR(255).
 */
public final class PasswordUtil {

    private static final int SALT_LENGTH_BYTES = 16;
    private static final int HASH_LENGTH_BITS = 256;
    private static final int ITERATIONS = 65536;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    private PasswordUtil() {
        // utility class, no instances
    }

    /** Hashes a plaintext password. Store the returned string as-is in Users.Password. */
    public static String hash(String plainPassword) {
        byte[] salt = generateSalt();
        byte[] hash = pbkdf2(plainPassword.toCharArray(), salt, ITERATIONS, HASH_LENGTH_BITS);
        return ITERATIONS + ":" + encode(salt) + ":" + encode(hash);
    }

    /**
     * Checks a plaintext password (login attempt) against a hash produced by hash().
     *
     * A malformed storedHash (wrong format, corrupt data, a placeholder left by
     * seed data, etc.) is treated as "does not match" rather than propagated as
     * an exception — a login attempt is user input territory, and callers should
     * never have to guard this call against a crash.
     */
    public static boolean verify(String plainPassword, String storedHash) {
        try {
            String[] parts = storedHash.split(":");
            if (parts.length != 3) {
                return false;
            }

            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = decode(parts[1]);
            byte[] expectedHash = decode(parts[2]);

            byte[] actualHash = pbkdf2(plainPassword.toCharArray(), salt, iterations, expectedHash.length * 8);

            // A plain == or Arrays.equals() would return faster for an
            // early mismatched byte than a full match — an attacker who
            // can measure response time could exploit that. Comparing
            // every byte regardless closes that side channel.
            return constantTimeEquals(expectedHash, actualHash);
        } catch (RuntimeException e) {
            // NumberFormatException, IllegalArgumentException (bad Base64), etc.
            return false;
        }
    }

    private static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLengthBits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLengthBits);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < a.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }

    private static String encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static byte[] decode(String str) {
        return Base64.getDecoder().decode(str);
    }
}
