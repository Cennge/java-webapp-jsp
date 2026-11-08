package step.learning.java231web.models;

import java.util.Map;

/**
 * Автентифікований користувач - дані з перевіреного токена (claims JWT).
 * AuthFilter кладе його до атрибута запиту "auth"
 * @author ilyag
 */
public record AuthUser(String id, String login, String name, String email, String role) {

    public static final String ATTRIBUTE = "auth";

    public static AuthUser fromClaims(Map<String, Object> claims) {
        return new AuthUser(
                str(claims.get("sub")),
                str(claims.get("login")),
                str(claims.get("name")),
                str(claims.get("email")),
                str(claims.get("role")));
    }

    private static String str(Object value) {
        return value == null ? null : value.toString();
    }
}
