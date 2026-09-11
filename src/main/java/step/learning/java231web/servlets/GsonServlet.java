package step.learning.java231web.servlets;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Робота з JSON. Бібліотека Gson
 * @author ilyag
 */
public class GsonServlet extends HttpServlet {

    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Data data = new Data()
                .setX(10)
                .setStr(null)
                .setB(true);
        // API режим - коли не переводимо до представлення, а
        // формуємо дані на рівні контролера.
        resp.setContentType("application/json");
        gson.toJson(data, resp.getWriter());
    }

}

class Data {
    private int x;
    private String str;
    private boolean b;

    public int getX() {
        return x;
    }

    public Data setX(int x) {
        this.x = x;
        return this;
    }

    public String getStr() {
        return str;
    }

    public Data setStr(String str) {
        this.str = str;
        return this;
    }

    public boolean isB() {
        return b;
    }

    public Data setB(boolean b) {
        this.b = b;
        return this;
    }
}
