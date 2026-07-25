package getticket.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Salted SHA-256 password hashing, so User.password is never stored or compared in plain text
 * (design doc requires passwords to be kept encrypted in the database).
 */
public final class PasswordUtil {

    private static final int SALT_BYTES = 16;

    private PasswordUtil() {
    }

    /** Produces a new "base64(salt):base64(hash)" string suitable for storing in Users.Password. */
    public static String hash(String rawPassword) {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] digest = digest(rawPassword, salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(digest);
    }

    /** Checks rawPassword against a hash previously produced by {@link #hash}. */
    public static boolean verify(String rawPassword, String storedHash) {
        String[] parts = storedHash.split(":", 2);
        if (parts.length != 2) {
            return false;
        }
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expectedDigest = Base64.getDecoder().decode(parts[1]);
        byte[] actualDigest = digest(rawPassword, salt);
        return MessageDigest.isEqual(expectedDigest, actualDigest);
    }

    private static byte[] digest(String rawPassword, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            return md.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
