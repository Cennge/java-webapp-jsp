package step.learning.java231web.servlets;

import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import step.learning.java231web.dao.UserDao;
import step.learning.java231web.models.UserSignupFormModel;
import step.learning.java231web.rest.RestPagination;
import step.learning.java231web.rest.RestResponse;
import step.learning.java231web.rest.RestServlet;
import step.learning.java231web.rest.RestStatus;
import step.learning.java231web.services.token.ITokenService;

/**
 * REST API користувачів:
 * POST /user            - реєстрація
 * GET  /user            - автентифікація (Authorization: Basic) -> токен
 * GET  /user/me         - перевірка токена (Authorization: Bearer)
 * GET  /user/available  - доступність логіна / email
 * GET  /user/access     - перелік доступу (логін, роль) з пагінацією, лише з токеном
 *
 * Кожен обробник лише формує RestResponse, відправляє його RestServlet.send().
 * Відповіді API користувачів не кешуються (містять токени та персональні дані)
 * @author ilyag
 */
@Singleton
public class UserServlet extends RestServlet {
    private static final System.Logger LOGGER = System.getLogger(UserServlet.class.getName());

    private final UserDao userDao;
    private final ITokenService tokenService;

    @Inject
    public UserServlet(UserDao userDao, ITokenService tokenService) {
        this.userDao = userDao;
        this.tokenService = tokenService;
    }

    @Override
    protected String getServiceName() {
        return "User API";
    }

    @Override
    protected String[] getManipulations() {
        return new String[] { "GET", "POST" };
    }

    @Override
    protected String getAuthChallenge(HttpServletRequest req) {
        String uri = req.getRequestURI();
        if (uri.endsWith("/user/me") || uri.endsWith("/user/access")) {
            return "Bearer";
        }
        // на Basic-виклик браузер показує власне вікно логіну; для запитів з JS
        // (X-Requested-With) воно не потрібне - форма сама показує помилку
        return req.getHeader("X-Requested-With") != null
                ? "FormBased"
                : "Basic realm=\"java-webapp\", charset=\"UTF-8\"";
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        send(req, resp, signup(req));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RestResponse restResponse = switch (pathTail(req)) {
            case "available" -> checkAvailability(req);
            case "me"        -> me(req);
            case "access"    -> access(req);
            default          -> authenticate(req);
        };
        send(req, resp, restResponse);
    }

    /**
     * GET /user з Authorization: Basic base64(login:password)
     * 200 - data: { token, user }
     * 401 - невірний логін / пароль (однаково для обох випадків)
     * 440 / 441 - відсутній / неправильний заголовок Authorization
     * 550 - помилка БД
     */
    private RestResponse authenticate(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null) {
            return error(RestStatus.HeaderRequired, "Authorization header required");
        }
        if (!authHeader.startsWith("Basic ")) {
            return error(RestStatus.HeaderMalformed, "Authorization scheme 'Basic' required");
        }
        String[] values;
        try {
            String credentials = new String(
                    Base64.getDecoder().decode(authHeader.substring("Basic ".length()).trim()),
                    StandardCharsets.UTF_8);
            values = credentials.split(":", 2);
        } catch (IllegalArgumentException ex) {
            values = new String[0];
        }
        if (values.length != 2) {
            return error(RestStatus.HeaderMalformed, "Credentials must be base64(login:password)");
        }

        Map<String, Object> user;
        try {
            userDao.install();
            user = userDao.authenticate(values[0], values[1]);
        } catch (Exception ex) {
            LOGGER.log(System.Logger.Level.ERROR, "authenticate", ex);
            // деталі помилки - лише у лог сервера, клієнту - загальне повідомлення
            return error(RestStatus.DatabaseError, "Authentication is temporarily unavailable");
        }
        if (user == null) {
            // однакова відповідь для "немає користувача" та "невірний пароль" -
            // не підказуємо, які логіни існують
            return error(RestStatus.Unauthorized, "Invalid login or password");
        }

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", user.get("id"));
        claims.put("name", user.get("name"));
        claims.put("email", user.get("email"));
        claims.put("login", user.get("login"));
        claims.put("role", user.get("role"));

        Map<String, Object> authData = new LinkedHashMap<>();
        authData.put("token", tokenService.create(claims));
        authData.put("user", user);

        RestResponse restResponse = new RestResponse(RestStatus.Ok, authData);
        restResponse.getMeta()
                .addLink("me", req.getContextPath() + "/user/me")
                .addLink("access", req.getContextPath() + "/user/access");
        return restResponse;
    }

    /**
     * GET /user/me з Authorization: Bearer token
     * 200 - data: claims токена; 444 / 445 / 446 - немає / прострочений / пошкоджений токен
     */
    private RestResponse me(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return error(RestStatus.TokenRequired, "Authorization: Bearer token required");
        }
        try {
            return new RestResponse(RestStatus.Ok,
                    tokenService.verify(authHeader.substring("Bearer ".length()).trim()));
        } catch (ITokenService.TokenException ex) {
            return error(ex.isExpired() ? RestStatus.TokenExpired : RestStatus.TokenMalformed, ex.getMessage());
        }
    }

    /**
     * GET /user/access?page=1&pageSize=5 з Authorization: Bearer token
     * Перелік доступу користувачів (логін, роль, ім'я, email) з пагінацією у meta.pagination.
     * Містить персональні дані, тому лише для автентифікованих (444/445/446 без токена)
     */
    private RestResponse access(HttpServletRequest req) {
        RestResponse auth = me(req);
        if (!auth.getStatus().isOk()) {
            return auth;
        }
        RestPagination pagination;
        try {
            pagination = paginationOf(req, 5);
        } catch (QueryParameterException ex) {
            return error(RestStatus.QueryParameterMalformed, ex.getMessage());
        }
        try {
            userDao.install();
            pagination.setTotalItems(userDao.getUsersCount());
            RestResponse restResponse = new RestResponse(RestStatus.Ok, userDao.getUsersAccess(pagination));
            restResponse.setPagination(pagination);
            restResponse.getMeta()
                    .addParameter("page", pagination.getPage())
                    .addParameter("pageSize", pagination.getPageSize());
            if (pagination.getPage() < pagination.getTotalPages()) {
                restResponse.getMeta().addLink("next", req.getContextPath() + "/user/access?page="
                        + (pagination.getPage() + 1) + "&pageSize=" + pagination.getPageSize());
            }
            if (pagination.getPage() > 1) {
                restResponse.getMeta().addLink("prev", req.getContextPath() + "/user/access?page="
                        + (pagination.getPage() - 1) + "&pageSize=" + pagination.getPageSize());
            }
            return restResponse;
        } catch (Exception ex) {
            LOGGER.log(System.Logger.Level.ERROR, "access", ex);
            return error(RestStatus.DatabaseError, "Access list is temporarily unavailable");
        }
    }

    /**
     * GET /user/available?login=... або ?email=...
     * Завжди 200 з { field, value, available }: "зайнято" - результат, а не помилка
     */
    private RestResponse checkAvailability(HttpServletRequest req) {
        String login = req.getParameter("login");
        String email = req.getParameter("email");
        String field = login != null ? "login" : email != null ? "email" : null;
        String value = login != null ? login : email;
        if (field == null || value.trim().isEmpty()) {
            return error(RestStatus.QueryParameterRequired, "Query parameter 'login' or 'email' required");
        }
        try {
            userDao.install();
            boolean available = "login".equals(field)
                    ? userDao.isLoginAvailable(value)
                    : userDao.isEmailAvailable(value);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("field", field);
            data.put("value", value.trim());
            data.put("available", available);
            return new RestResponse(RestStatus.Ok, data);
        } catch (Exception ex) {
            LOGGER.log(System.Logger.Level.ERROR, "checkAvailability", ex);
            return error(RestStatus.DatabaseError, "Availability check is temporarily unavailable");
        }
    }

    /**
     * POST /user - реєстрація
     * 201 - створено; 447 - помилки валідації по полях; 448 - конфлікт унікальності
     */
    private RestResponse signup(HttpServletRequest req) {
        UserSignupFormModel formModel;
        try (BufferedReader reader = req.getReader()) {
            formModel = gson.fromJson(reader.lines().collect(Collectors.joining("\n")), UserSignupFormModel.class);
        } catch (IOException | JsonSyntaxException ex) {
            return error(RestStatus.BadRequest, "Body must be JSON");
        }
        if (formModel == null) {
            return error(RestStatus.BadRequest, "Body is empty");
        }

        try {
            // валідація моделі + унікальність логіна / email: помилки по полях.
            // Для полів з помилкою формату звертатись до БД немає сенсу
            Map<String, String> errors = new LinkedHashMap<>(formModel.getValidationErrors());
            userDao.install();
            userDao.getUniquenessErrors(formModel).forEach(errors::putIfAbsent);
            if (!errors.isEmpty()) {
                return new RestResponse(RestStatus.ValidationFailed, errors);
            }
            userDao.signupUser(formModel);
        } catch (IllegalArgumentException ex) {
            // логін/email зайняли між перевіркою та вставкою (конкурентний запит)
            return error(RestStatus.LoginOccupied, ex.getMessage());
        } catch (Exception ex) {
            LOGGER.log(System.Logger.Level.ERROR, "signup", ex);
            return error(RestStatus.DatabaseError, "Registration is temporarily unavailable");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", formModel.getName());
        result.put("email", formModel.getEmail());
        // без логіна логіном у БД стає ім'я (див. UserDao.signupUser)
        result.put("login", formModel.getLogin() != null && !formModel.getLogin().trim().isEmpty()
                ? formModel.getLogin().trim() : formModel.getName().trim());
        result.put("message", "User registered successfully");

        RestResponse restResponse = new RestResponse(RestStatus.Created, result);
        restResponse.getMeta().addLink("authenticate", req.getContextPath() + "/user");
        return restResponse;
    }
}
