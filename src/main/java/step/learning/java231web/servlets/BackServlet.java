package step.learning.java231web.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import step.learning.java231web.rest.RestResponse;
import step.learning.java231web.rest.RestStatus;

@Singleton
public class BackServlet extends HttpServlet {
    private final Gson gson;

    @Inject
    public BackServlet() {
        this.gson = new GsonBuilder().serializeNulls().create();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getMethod();
        if ("PATCH".equalsIgnoreCase(method)) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String acceptHeader = req.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains("text/html") && req.getParameter("raw") == null) {
            req.setAttribute("servlet", "Back");
            req.getRequestDispatcher("index.jsp").forward(req, resp);
            return;
        }
        processRequest(req, resp, "GET");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp, "POST");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp, "PUT");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp, "DELETE");
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp, "PATCH");
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp, String method) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        String body = "";
        try (BufferedReader reader = req.getReader()) {
            body = reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception ignored) {
        }

        Map<String, String[]> parameterMap = req.getParameterMap();
        Map<String, String> singleParams = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            singleParams.put(entry.getKey(), String.join(", ", entry.getValue()));
        }

        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration<String> headerNames = req.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headers.put(headerName, req.getHeader(headerName));
            }
        }

        Map<String, Object> responseData = new LinkedHashMap<>();
        responseData.put("method", method);
        responseData.put("url", req.getRequestURI());
        responseData.put("parameters", singleParams);
        responseData.put("headers", headers);
        responseData.put("body", body);
        responseData.put("timestamp", System.currentTimeMillis());

        RestResponse restResponse = new RestResponse(RestStatus.Ok, responseData);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().print(gson.toJson(restResponse));
    }
}
