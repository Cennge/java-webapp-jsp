package step.learning.java231web.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Singleton;
import jakarta.servlet.ServletException;
// import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.Date;

/**
 * Робота з JSON. Бібліотека Gson
 * @author ilyag
 */
// @WebServlet("/gson")
@Singleton
public class GsonServlet extends HttpServlet {

    private final Gson gson = new GsonBuilder()
            .serializeNulls()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Data data = new Data()
                .setX(10)
                .setStr(null)
                .setCyr("illia")
                .setB(true)
                .setDate(new Date());
        // API режим - коли не переводимо до представлення, а
        // формуємо дані на рівні контролера.
        resp.setContentType("application/json");
        // ?sections - віддаємо вміст файлу-ресурсу sections.json
        if (req.getParameter("sections") != null) {
            resp.getWriter().print(readResource("sections.json"));
            return;
        }
        gson.toJson(data, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // десеріалізація: JSON з тіла запиту -> об'єкт Data -> JSON у відповідь
        String body = req.getReader().lines().collect(Collectors.joining("\n"));
        Data data = gson.fromJson(body, Data.class);
        resp.setContentType("application/json");
        gson.toJson(data, resp.getWriter());
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Data data = new Data()
                .setX(20)
                .setStr("PUT")
                .setCyr("Ґранд PUT")
                .setB(false)
                .setDate(new Date());
        resp.setContentType("application/json");
        gson.toJson(data, resp.getWriter());
    }

    private String readResource(String name) {
        try (BufferedReader rdr = new BufferedReader(new InputStreamReader(
                this.getClass().getClassLoader().getResourceAsStream(name),
                StandardCharsets.UTF_8))) {
            return rdr.lines().collect(Collectors.joining("\n"));
        } catch (Exception ex) {
            return "\"Error\"";
        }
    }

}

class Data {
    private int x;
    private String str;
    private String cyr;
    private boolean b;
    private Date date;

    public Date getDate() {
        return date;
    }

    public Data setDate(Date date) {
        this.date = date;
        return this;
    }

    public String getCyr() {
        return cyr;
    }

    public Data setCyr(String cyr) {
        this.cyr = cyr;
        return this;
    }

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
