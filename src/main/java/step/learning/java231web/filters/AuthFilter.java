package step.learning.java231web.filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * Фільтр перевірки авторизації
 * @author ilyag
 */
@WebFilter("/*")  // анотації спрощують реєстрацію фільтрів, але
// !! не дозволяють регулювати їх порядок
// тоді як web.xml чи IoC дозволяють
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest sr, ServletResponse sr1, FilterChain fc)
            throws IOException, ServletException {
        sr.setAttribute("auth", "checked");
        fc.doFilter(sr, sr1);
    }

}
