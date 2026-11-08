package step.learning.java231web.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import step.learning.java231web.services.db.IDbService;

@Singleton
public class DbServlet extends HttpServlet {
    private final IDbService _dbService;

    @Inject
    public DbServlet(IDbService dbService) {
        this._dbService = dbService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("servlet", "Db");

        String connectionStatus = "Не підключено";
        String ddlResult = null;
        List<String> dmlRows = new ArrayList<>();
        String param = req.getParameter("param");
        String paramResult = null;
        String uuid = null;

        try {
            Connection connection = _dbService.getConnection();
            if (connection != null && !connection.isClosed()) {
                connectionStatus = "Ok";
            }

            if (connection != null) {
                try (Statement statement = connection.createStatement()) {
                    String ddl = "CREATE TABLE IF NOT EXISTS j231_queries ( "
                            + "id CHAR(36) PRIMARY KEY, "
                            + "query_time DATETIME, "
                            + "query_date DATE "
                            + ")";
                    statement.executeUpdate(ddl);
                    ddlResult = "Query OK";

                    String dmlInsert = "INSERT INTO j231_queries (id, query_time, query_date) "
                            + "VALUES (UUID(), NOW(), CURRENT_DATE())";
                    statement.executeUpdate(dmlInsert);

                    String dmlSelect = "SELECT query_time, query_date FROM j231_queries "
                            + "ORDER BY query_time DESC LIMIT 2";
                    try (ResultSet rs = statement.executeQuery(dmlSelect)) {
                        while (rs.next()) {
                            Timestamp ts = rs.getTimestamp("query_time");
                            Date dt = rs.getDate("query_date");
                            String tsStr = ts != null ? ts.toInstant().toString() : "";
                            String dtStr = dt != null ? dt.toString() : "";
                            dmlRows.add(tsStr + " " + dtStr);
                        }
                    }

                    String uuidSql = "SELECT UUID() AS uuid";
                    try (ResultSet rs = statement.executeQuery(uuidSql)) {
                        if (rs.next()) {
                            uuid = rs.getString("uuid");
                        }
                    }
                }

                if (param != null) {
                    String prepSql = "SELECT ? AS param_val";
                    try (PreparedStatement prep = connection.prepareStatement(prepSql)) {
                        prep.setString(1, param);
                        try (ResultSet rs = prep.executeQuery()) {
                            if (rs.next()) {
                                paramResult = rs.getString(1);
                            }
                        }
                    }
                }

                req.setAttribute("dbSuccess", true);
            }
        } catch (Exception ex) {
            connectionStatus = ex.getMessage();
            req.setAttribute("dbSuccess", false);
            req.setAttribute("dbError", ex.getMessage());
        }

        req.setAttribute("connectionStatus", connectionStatus);
        req.setAttribute("ddlResult", ddlResult);
        req.setAttribute("dmlRows", dmlRows);
        req.setAttribute("param", param);
        req.setAttribute("paramResult", paramResult);
        req.setAttribute("uuid", uuid);
        req.setAttribute("sqlQuery", "SELECT UUID() AS uuid;");

        req.getRequestDispatcher("index.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
