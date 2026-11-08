package step.learning.java231web.filters;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import step.learning.java231web.models.AuthUser;
import step.learning.java231web.services.token.ITokenService;

/**
 * Фільтр автентифікації: шукає токен у заголовку "Authorization: Bearer ..."
 * або у параметрі запиту ?ua=... (user authorization), перевіряє його та кладе
 * до атрибута "auth" користувача (AuthUser). Без токена або з недійсним токеном
 * атрибут лишається null, а причина - в атрибуті "authError".
 * Фільтр не забороняє доступ: це вирішують сервлети, яким потрібна авторизація
 * @author ilyag
 */
@Singleton
// @WebFilter("/*")  // анотації спрощують реєстрацію фільтрів, але
// !! не дозволяють регулювати їх порядок
// тоді як web.xml чи IoC дозволяють
public class AuthFilter implements Filter {
    public static final String ERROR_ATTRIBUTE = "authError";

    private final ITokenService tokenService;

    @Inject
    public AuthFilter(ITokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public void doFilter(ServletRequest sr, ServletResponse sr1, FilterChain fc)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) sr;
        String token = tokenOf(req);
        if (token != null) {
            try {
                req.setAttribute(AuthUser.ATTRIBUTE, AuthUser.fromClaims(tokenService.verify(token)));
            } catch (ITokenService.TokenException ex) {
                req.setAttribute(ERROR_ATTRIBUTE, ex);
            }
        }
        fc.doFilter(sr, sr1);
    }

    /** Токен із заголовка Authorization: Bearer ... або з параметра ?ua= */
    static String tokenOf(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        if (header != null && header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            String token = header.substring(7).trim();
            return token.isEmpty() ? null : token;
        }
        // ua - лише з рядка запиту: getParameter() прочитав би тіло POST-форми
        // раніше за сервлет, і той отримав би порожнє тіло
        String query = req.getQueryString();
        if (query == null) {
            return null;
        }
        for (String pair : query.split("&")) {
            if (pair.startsWith("ua=") && pair.length() > 3) {
                return java.net.URLDecoder.decode(pair.substring(3), java.nio.charset.StandardCharsets.UTF_8).trim();
            }
        }
        return null;
    }
}
