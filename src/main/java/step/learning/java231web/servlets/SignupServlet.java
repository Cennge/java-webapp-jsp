package step.learning.java231web.servlets;

import com.google.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Сторінка форми реєстрації користувача.
 * Дані форми надсилаються (fetch) на API "/user", де проходять валідацію
 * моделлю UserSignupFormModel
 * @author ilyag
 */
@Singleton
public class SignupServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("servlet", "Signup");
        req.getRequestDispatcher("index.jsp")
                .forward(req, resp);
    }

}
