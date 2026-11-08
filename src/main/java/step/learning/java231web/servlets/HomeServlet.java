package step.learning.java231web.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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

    @Inject  // необхідна анотація для інжекційного конструктора
    public HomeServlet(IHashService hashService, ITimeService timeService) {
        this._hashService = hashService;
        this._timeService = timeService;
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
        // передаємо управління на представлення (~return View("index.jsp"))
        req.getRequestDispatcher("index.jsp")
                .forward(req, resp);
    }

}
