package step.learning.java231web.services.token;

import java.util.Map;

/**
 * Токени автентифікації (JWT)
 * @author ilyag
 */
public interface ITokenService {

    /** Підписаний токен з даними (claims) та терміном дії */
    String create(Map<String, Object> claims);

    /**
     * Перевірка токена
     * @return claims токена
     * @throws TokenException якщо токен пошкоджений, з чужим підписом або прострочений
     */
    Map<String, Object> verify(String token) throws TokenException;

    class TokenException extends Exception {
        private final boolean expired;

        public TokenException(String message, boolean expired) {
            super(message);
            this.expired = expired;
        }

        public boolean isExpired() {
            return expired;
        }
    }

}
