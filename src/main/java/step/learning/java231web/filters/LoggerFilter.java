package step.learning.java231web.filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Фільтр-логер: виводить на консоль дату-час та адресу кожного запиту
 * @author ilyag
 */
@WebFilter("/*")
public class LoggerFilter implements Filter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void doFilter(ServletRequest sr, ServletResponse sresp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) sr;

        String dateTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        String address = req.getRequestURI();
        String queryString = req.getQueryString();
        if (queryString != null) {
            address += "?" + queryString;
        }

        System.out.println("[" + dateTime + "] " + req.getMethod() + " " + address);

        chain.doFilter(sr, sresp);
    }

}
