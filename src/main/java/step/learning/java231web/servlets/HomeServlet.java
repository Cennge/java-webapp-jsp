package step.learning.java231web.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;
import step.learning.java231web.dao.UserDao;
import step.learning.java231web.filters.AuthFilter;
import step.learning.java231web.models.AuthUser;
import step.learning.java231web.services.hash.IHashService;
import step.learning.java231web.services.time.ITimeService;

/**
 *
 * @author ilyag
 */
@Singleton
public class HomeServlet extends HttpServlet {

    private final IHashService _hashService;
    private final ITimeService _timeService;
    private final UserDao _userDao;

    @Inject  // необхідна анотація для інжекційного конструктора
    public HomeServlet(IHashService hashService, ITimeService timeService, UserDao userDao) {
        this._hashService = hashService;
        this._timeService = timeService;
        this._userDao = userDao;
    }
    /*
    Д.З. Впровадити у проект сервіс часу (timestamp),
    який буде формувати мітку часу поточного моменту
    */

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // закладаємо до req додатковий атрибут, який дасть можливість
        // контролю факту проходження запиту через сервлет
        req.setAttribute("servlet", "Home");
        req.setAttribute("hash", _hashService.hexDigest("123"));
        req.setAttribute("timestamp10", _timeService.getTimestamp10());
        req.setAttribute("timestamp13", _timeService.getTimestamp13());
        checkUaById(req);
        // передаємо управління на представлення (~return View("index.jsp"))
        req.getRequestDispatcher("index.jsp")
                .forward(req, resp);
    }

    /*
    Д.З. Реалізувати можливість перевірки авторизації: параметр
    /?ua=6aa5bcec-8f16-4d15-95ba-b67b9d63bdff - за наявності та правильності
    звіт про авторизацію містить ідентифікатор користувача та його ім'я.
    JWT-токен у ?ua= перевіряє AuthFilter; тут - варіант, коли ua є id (UUID)
    */
    private void checkUaById(HttpServletRequest req) {
        String ua = req.getParameter("ua");
        if (ua == null || req.getAttribute(AuthUser.ATTRIBUTE) != null) {
            return;
        }
        ua = ua.trim();
        try {
            UUID.fromString(ua);
        } catch (IllegalArgumentException ex) {
            return;  // не UUID - лишаємо результат AuthFilter (помилка токена)
        }
        try {
            AuthUser user = _userDao.getAuthUserById(ua);
            if (user != null) {
                req.setAttribute(AuthUser.ATTRIBUTE, user);
                req.removeAttribute(AuthFilter.ERROR_ATTRIBUTE);
                req.setAttribute("authSource", "id");
            } else {
                req.setAttribute(AuthFilter.ERROR_ATTRIBUTE,
                        new IllegalArgumentException("користувача з id " + ua + " не знайдено"));
            }
        } catch (SQLException ex) {
            req.setAttribute(AuthFilter.ERROR_ATTRIBUTE, new IllegalStateException("помилка БД: " + ex.getMessage()));
        }
    }
}
