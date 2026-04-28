package SourceCode.UserInstances;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import java.util.Base64;
import java.util.Arrays;

public class PasswordUtil {

    /**
     * Hashes the password from a JPasswordField char array.
     * Usage: hashPassword(passwordPF.getPassword())
     */
    public static String hashPassword(char[] passwordChars) {
        byte[] salt = generateSalt();

        Argon2Parameters params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withSalt(salt)
                .withIterations(3)
                .withMemoryAsKB(65536)
                .withParallelism(2)
                .build();

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(params);

        byte[] hash = new byte[32];
        generator.generateBytes(passwordChars, hash);

        // Clear the char array from memory after use
        Arrays.fill(passwordChars, '\0');

        String saltB64 = Base64.getEncoder().encodeToString(salt);
        String hashB64 = Base64.getEncoder().encodeToString(hash);
        return saltB64 + ":" + hashB64;
    }

    /**
     * Verifies the password from a JPasswordField char array.
     * Usage: verifyPassword(passwordPF.getPassword(), storedHash)
     */
    public static boolean verifyPassword(char[] passwordChars, String stored) {
        String[] parts = stored.split(":");
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

        Argon2Parameters params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withSalt(salt)
                .withIterations(3)
                .withMemoryAsKB(65536)
                .withParallelism(2)
                .build();

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(params);

        byte[] actualHash = new byte[32];
        generator.generateBytes(passwordChars, actualHash);

        // Clear the char array from memory after use
        Arrays.fill(passwordChars, '\0');

        return Arrays.equals(expectedHash, actualHash);
    }

    private static byte[] generateSalt() {
        byte[] salt = new byte[16];
        new java.security.SecureRandom().nextBytes(salt);
        return salt;
    }
}