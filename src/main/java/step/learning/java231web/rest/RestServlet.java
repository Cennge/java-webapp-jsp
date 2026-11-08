package step.learning.java231web.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Базовий клас REST-сервлетів: обробники лише формують RestResponse,
 * а відправляє його один метод send():
 * - meta: serviceName, url, method, dataType, manipulations (якщо не задані обробником)
 * - HTTP-статус відповідно до RestStatus: стандартний код - як є; нестандартні
 *   440/441 (заголовок Authorization) та 444-446 (токен) - 401, інші 44x - 400,
 *   55x - 500 (точний код - у status.code)
 * - 401: заголовок WWW-Authenticate (вимога RFC 9110)
 * - Cache-Control за meta.cacheTime (0 - не кешувати)
 * @author ilyag
 */
public abstract class RestServlet extends HttpServlet {

    protected final Gson gson = new GsonBuilder()
            .serializeNulls()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    /** Назва сервісу для meta.serviceName */
    protected abstract String getServiceName();

    /** HTTP-методи, що підтримує сервіс (meta.manipulations) */
    protected String[] getManipulations() {
        return new String[] { "GET" };
    }

    /** Значення WWW-Authenticate для відповідей 401 */
    protected String getAuthChallenge(HttpServletRequest req) {
        return "Bearer";
    }

    protected RestResponse error(RestStatus status, String message) {
        return new RestResponse(status, message);
    }

    protected void send(HttpServletRequest req, HttpServletResponse resp, RestResponse restResponse) throws IOException {
        RestStatus status = restResponse.getStatus();
        RestMeta meta = restResponse.getMeta();
        if (meta.getServiceName() == null) {
            meta.setServiceName(getServiceName());
        }
        if (meta.getDataType() == null) {
            meta.setDataType(RestMeta.typeOf(restResponse.getData()));
        }
        if (meta.getManipulations() == null) {
            meta.setManipulations(getManipulations());
        }
        meta.setUrl(req.getRequestURI())
            .setMethod(req.getMethod());

        int httpCode = httpCodeOf(status);
        resp.setStatus(httpCode);
        if (httpCode == HttpServletResponse.SC_UNAUTHORIZED) {
            resp.setHeader("WWW-Authenticate", getAuthChallenge(req));
        }
        resp.setHeader("Cache-Control", status.isOk() && meta.getCacheTime() > 0
                ? "public, max-age=" + meta.getCacheTime()
                : "no-store");
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().print(gson.toJson(restResponse));
    }

    public static int httpCodeOf(RestStatus status) {
        int code = status.getCode();
        if (status.isStandard()) {
            return code;
        }
        // 440/441 - заголовок Authorization, 444-446 - токен: це проблеми автентифікації
        if (code == 440 || code == 441 || (code >= 444 && code <= 446)) {
            return HttpServletResponse.SC_UNAUTHORIZED;
        }
        return (code / 100) * 100;
    }

    /**
     * Пагінація з параметрів запиту ?page=..&pageSize=.. (синоніми pageSize: perPage, size)
     * @throws QueryParameterException якщо параметр не число або поза межами
     */
    protected RestPagination paginationOf(HttpServletRequest req, int defaultPageSize) throws QueryParameterException {
        int page = intParam(req, "page", 1);
        String sizeName = req.getParameter("pageSize") != null ? "pageSize"
                : req.getParameter("perPage") != null ? "perPage"
                : "size";
        int pageSize = intParam(req, sizeName, defaultPageSize);
        if (page < 1) {
            throw new QueryParameterException("Query parameter 'page' must be greater than 0");
        }
        if (pageSize < 1 || pageSize > RestPagination.MAX_PAGE_SIZE) {
            throw new QueryParameterException("Query parameter '" + sizeName + "' must be between 1 and "
                    + RestPagination.MAX_PAGE_SIZE);
        }
        return new RestPagination(page, pageSize, 0);
    }

    protected int intParam(HttpServletRequest req, String name, int defaultValue) throws QueryParameterException {
        String value = req.getParameter(name);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            throw new QueryParameterException("Query parameter '" + name + "' must be a valid integer");
        }
    }

    /** Абсолютна адреса файлу зі сховища: http://host:port/context/storage/{file} */
    protected static String storageUrl(HttpServletRequest req, String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }
        if (fileName.startsWith("http://") || fileName.startsWith("https://")) {
            return fileName;
        }
        return req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort()
                + req.getContextPath() + "/storage/" + fileName;
    }

    /** Значення шляху після адреси сервлета без крайніх "/": /section/tv/ -> "tv" */
    protected static String pathTail(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            return "";
        }
        String tail = pathInfo.trim();
        while (tail.startsWith("/")) tail = tail.substring(1);
        while (tail.endsWith("/")) tail = tail.substring(0, tail.length() - 1);
        return tail;
    }

    /** Помилка у параметрі запиту -> 443 Query Parameter Malformed */
    public static class QueryParameterException extends Exception {
        public QueryParameterException(String message) {
            super(message);
        }
    }
}
