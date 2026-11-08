package step.learning.java231web.services.kdf;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.HexFormat;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * PBKDF2 (HMAC-SHA256) - стандартний "повільний" KDF, вбудований у JDK.
 * Велика кількість ітерацій ускладнює перебір паролів
 * @author ilyag
 */
public class Pbkdf2KdfService implements IKdfService {

    private static final int ITERATIONS = 100_000;
    private static final int KEY_BITS = 256;         // 32 байти -> 64 hex-символи
    private static final int SALT_BYTES = 16;        // 16 байт  -> 32 hex-символи

    private final SecureRandom random = new SecureRandom();

    @Override
    public String newSalt() {
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        return HexFormat.of().formatHex(salt);
    }

    @Override
    public String dk(String password, String salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    HexFormat.of().parseHex(salt),
                    ITERATIONS,
                    KEY_BITS);
            byte[] key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();
            spec.clearPassword();
            return HexFormat.of().formatHex(key);
        } catch (GeneralSecurityException ex) {
            throw new RuntimeException("PBKDF2 is not available", ex);
        }
    }

}
