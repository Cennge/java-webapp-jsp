package step.learning.java231web.services.hash;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author ilyag
 */
public class Md5HashService implements IHashService {

    private final char[] digits = "0123456789abcdef".toCharArray();

    @Override
    public String hexDigest(String input) {
        try {
            // 1. Get an MD5 MessageDigest instance
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 2. Compute the hash bytes from the input string
            byte[] hashBytes = md.digest(
                    input.getBytes( StandardCharsets.UTF_8 )
            );

            // 3. Convert the byte array to a hexadecimal string
            char[] hexChars = new char[32];
            int i = 0;
            for (byte b : hashBytes) {
                hexChars[i++] = digits[ (b >> 4) & 0x0F ];
                hexChars[i++] = digits[ b & 0x0F ];
            }

            return new String( hexChars );
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

}
