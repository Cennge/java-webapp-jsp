package step.learning.java231web.filters;

import com.google.inject.Singleton;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * CORS settings
 * @author ilyag
 */
@Singleton
public class CorsFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest sr,
            ServletResponse sr1,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) sr;
        HttpServletResponse resp = (HttpServletResponse) sr1;

        chain.doFilter(sr, sr1);

        resp.setHeader("Access-Control-Allow-Origin", "*");
        String reqMethod = req.getHeader("access-control-request-method");
        if(reqMethod != null) {
            resp.setHeader("Access-Control-Allow-Methods", reqMethod);
        }
        String reqHeaders = req.getHeader("access-control-request-headers");
        if(reqHeaders != null) {
            resp.setHeader("Access-Control-Allow-Headers", reqHeaders);
        }
    }

}