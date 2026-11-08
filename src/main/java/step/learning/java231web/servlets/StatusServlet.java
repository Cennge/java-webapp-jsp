package step.learning.java231web.servlets;

import com.google.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import step.learning.java231web.rest.RestResponse;
import step.learning.java231web.rest.RestServlet;
import step.learning.java231web.rest.RestStatus;

/**
 * Довідник статусів REST API:
 * GET /status          - усі статуси (стандартні та нестандартні)
 * GET /status?code=447 - один статус за кодом
 * @author ilyag
 */
@Singleton
public class StatusServlet extends RestServlet {

    @Override
    protected String getServiceName() {
        return "API Statuses";
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        send(req, resp, statuses(req));
    }

    private RestResponse statuses(HttpServletRequest req) {
        String codeParam = req.getParameter("code");
        if (codeParam == null) {
            List<Map<String, Object>> all = RestStatus.values().stream()
                    .map(this::describe)
                    .collect(Collectors.toList());
            RestResponse restResponse = new RestResponse(RestStatus.Ok, all);
            restResponse.getMeta().setCacheTime(3600);   // довідник змінюється лише з новою версією
            return restResponse;
        }

        int code;
        try {
            code = Integer.parseInt(codeParam.trim());
        } catch (NumberFormatException ex) {
            return error(RestStatus.QueryParameterMalformed, "Parameter 'code' must be integer, got '" + codeParam + "'");
        }
        RestStatus status = RestStatus.fromCode(code);
        if (status == null) {
            return new RestResponse(RestStatus.NotFound.withMessage("Status " + code + " not found"), null);
        }
        RestResponse restResponse = new RestResponse(RestStatus.Ok, describe(status));
        restResponse.getMeta().addParameter("code", code).setCacheTime(3600);
        return restResponse;
    }

    private Map<String, Object> describe(RestStatus status) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("code", status.getCode());
        m.put("message", status.getMessage());
        m.put("isOk", status.isOk());
        m.put("standard", status.isStandard());
        return m;
    }
}
