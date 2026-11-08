package step.learning.java231web.util;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Передача управління на представлення (загальний шаблон index.jsp)
 * @author ilyag
 */
public final class Views {

    private Views() {
    }

    /**
     * forward на index.jsp з урахуванням особливості Guice: для шаблонів "/x/*"
     * Guice обгортає запит і повертає власний getServletPath() = "/x". Обгортка лишається
     * і після forward, тоді JSP-сервлет Tomcat шукає файл "/x" замість "/index.jsp".
     * Тому передаємо далі запит контейнера (без обгорток Guice)
     */
    public static void layout(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ServletRequest containerRequest = req;
        while (containerRequest instanceof ServletRequestWrapper wrapper) {
            containerRequest = wrapper.getRequest();
        }
        req.getServletContext().getRequestDispatcher("/index.jsp").forward(containerRequest, resp);
    }
}
