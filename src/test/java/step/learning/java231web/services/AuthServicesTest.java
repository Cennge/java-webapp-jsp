package step.learning.java231web.services;

import org.junit.jupiter.api.Test;
import step.learning.java231web.services.kdf.IKdfService;
import step.learning.java231web.services.kdf.Pbkdf2KdfService;
import step.learning.java231web.services.time.ITimeService;
import step.learning.java231web.services.time.SystemTimeService;
import step.learning.java231web.services.token.ITokenService;
import step.learning.java231web.services.token.JwtTokenService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServicesTest {

    private final IKdfService kdf = new Pbkdf2KdfService();

    @Test
    public void testKdfIsDeterministicForSameSalt() {
        String salt = kdf.newSalt();
        assertEquals(32, salt.length());
        String dk = kdf.dk("Secret123", salt);
        assertEquals(64, dk.length());
        assertEquals(dk, kdf.dk("Secret123", salt));
        assertNotEquals(dk, kdf.dk("Secret124", salt));
    }

    @Test
    public void testKdfDependsOnSalt() {
        String s1 = kdf.newSalt(), s2 = kdf.newSalt();
        assertNotEquals(s1, s2);
        assertNotEquals(kdf.dk("Secret123", s1), kdf.dk("Secret123", s2));
        assertDoesNotThrow(() -> kdf.dk("Пароль123", s1));
    }

    @Test
    public void testTokenRoundTrip() throws Exception {
        ITokenService tokens = new JwtTokenService(new SystemTimeService());
        String token = tokens.create(Map.of("sub", "42", "name", "Ілля"));
        assertEquals(3, token.split("\\.").length);

        Map<String, Object> claims = tokens.verify(token);
        assertEquals("42", claims.get("sub"));
        assertEquals("Ілля", claims.get("name"));
        assertInstanceOf(Long.class, claims.get("exp"));
    }

    @Test
    public void testTamperedOrForeignTokenRejected() {
        ITokenService tokens = new JwtTokenService(new SystemTimeService());
        String token = tokens.create(Map.of("sub", "42"));
        String[] p = token.split("\\.");
        String forged = p[0] + "." + p[1].substring(0, p[1].length() - 2) + "AA." + p[2];

        var ex = assertThrows(ITokenService.TokenException.class, () -> tokens.verify(forged));
        assertFalse(ex.isExpired());
        // токен від іншого екземпляра (інший секрет) - невалідний
        ITokenService other = new JwtTokenService(new SystemTimeService());
        assertThrows(ITokenService.TokenException.class, () -> other.verify(token));
        assertThrows(ITokenService.TokenException.class, () -> tokens.verify("abc"));
        assertThrows(ITokenService.TokenException.class, () -> tokens.verify(null));
    }

    @Test
    public void testExpiredTokenRejected() {
        // "годинник", що йде на 2 години вперед після видачі токена
        long[] now = { 1_790_000_000L };
        ITimeService clock = new SystemTimeService() {
            @Override
            public long getTimestamp10() { return now[0]; }
        };
        ITokenService tokens = new JwtTokenService(clock);
        String token = tokens.create(Map.of("sub", "42"));
        now[0] += 2 * 3600;

        var ex = assertThrows(ITokenService.TokenException.class, () -> tokens.verify(token));
        assertTrue(ex.isExpired());
    }
}
