package step.learning.java231web.services.token;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.ToNumberPolicy;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import step.learning.java231web.services.time.ITimeService;

/**
 * JWT з підписом HS256 (HMAC-SHA256): header.payload.signature
 * Секрет генерується при старті застосунку, тому після перезапуску
 * сервера раніше видані токени стають недійсними (потрібен повторний вхід)
 * @author ilyag
 */
@Singleton
public class JwtTokenService implements ITokenService {

    private static final long LIFETIME_SECONDS = 3600;   // 1 година
    private static final String HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

    private final ITimeService timeService;
    private final byte[] secret = new byte[32];
    // LONG_OR_DOUBLE: цілі числа (iat, exp) читаються як long, а не 1.79E9
    private final Gson gson = new GsonBuilder()
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .create();
    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    private final Base64.Decoder decoder = Base64.getUrlDecoder();

    @Inject
    public JwtTokenService(ITimeService timeService) {
        this.timeService = timeService;
        new SecureRandom().nextBytes(secret);
    }

    @Override
    public String create(Map<String, Object> claims) {
        Map<String, Object> payload = new LinkedHashMap<>(claims);
        long now = timeService.getTimestamp10();
        payload.put("iat", now);
        payload.put("exp", now + LIFETIME_SECONDS);
        String body = b64(HEADER) + "." + b64(gson.toJson(payload));
        return body + "." + encoder.encodeToString(sign(body));
    }

    @Override
    public Map<String, Object> verify(String token) throws TokenException {
        String[] parts = token == null ? new String[0] : token.split("\\.");
        if (parts.length != 3) {
            throw new TokenException("Token must have 3 parts", false);
        }
        byte[] signature;
        Map<String, Object> payload;
        try {
            signature = decoder.decode(parts[2]);
            payload = gson.fromJson(
                    new String(decoder.decode(parts[1]), StandardCharsets.UTF_8),
                    new TypeToken<Map<String, Object>>() {}.getType());
        } catch (IllegalArgumentException | JsonSyntaxException ex) {
            throw new TokenException("Token is not valid Base64/JSON", false);
        }
        // порівняння за сталий час - захист від timing-атак
        if (!MessageDigest.isEqual(signature, sign(parts[0] + "." + parts[1]))) {
            throw new TokenException("Token signature is invalid", false);
        }
        Object exp = payload == null ? null : payload.get("exp");
        if (!(exp instanceof Number) || ((Number) exp).longValue() < timeService.getTimestamp10()) {
            throw new TokenException("Token expired", true);
        }
        return payload;
    }

    private String b64(String s) {
        return encoder.encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException ex) {
            throw new RuntimeException("HmacSHA256 is not available", ex);
        }
    }

}
