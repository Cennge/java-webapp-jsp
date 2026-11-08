package step.learning.java231web.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Статус REST-відповіді: ознака успіху, код, повідомлення.
 * Набір статичних полів - "перелік" стандартних (RFC 9110) та
 * нестандартних (власних для проєкту) статусів.
 * Статичні поля незмінні (frozen): сеттери на них кидають виняток,
 * інакше зміна, наприклад, RestStatus.Ok.setMessage(...) зіпсувала б
 * статус для всього застосунку. Для власного тексту - withMessage(...)
 * @author ilyag
 */
public class RestStatus {
    // реєстр усіх статичних статусів: code -> status (порядок оголошення).
    // Має бути оголошений ДО полів-статусів (порядок статичної ініціалізації)
    private static final Map<Integer, RestStatus> REGISTRY = new LinkedHashMap<>();

    private boolean isOk;
    private int code;
    private String message;
    private transient boolean frozen;   // transient - не серіалізується у JSON

    public RestStatus() {
    }

    public RestStatus(boolean isOk, int code, String message) {
        this.isOk = isOk;
        this.code = code;
        this.message = message;
    }

    private static RestStatus of(int code, String message) {
        RestStatus status = new RestStatus(code < 400, code, message);
        status.frozen = true;
        REGISTRY.put(code, status);
        return status;
    }

    // ----- STANDARD: 2xx Success --------
    public static final RestStatus Ok                  = of(200, "OK");
    public static final RestStatus Created             = of(201, "Created");
    public static final RestStatus Accepted            = of(202, "Accepted");
    public static final RestStatus NoContent           = of(204, "No Content");
    public static final RestStatus AlreadyReported     = of(208, "Already Reported");
    public static final RestStatus PartialContent      = of(206, "Partial Content");

    // ----- STANDARD: 3xx Redirection --------
    public static final RestStatus MovedPermanently    = of(301, "Moved Permanently");
    public static final RestStatus Found               = of(302, "Found");
    public static final RestStatus SeeOther            = of(303, "See Other");
    public static final RestStatus NotModified         = of(304, "Not Modified");
    public static final RestStatus TemporaryRedirect   = of(307, "Temporary Redirect");
    public static final RestStatus PermanentRedirect   = of(308, "Permanent Redirect");

    // ----- STANDARD: 4xx Client Error --------
    public static final RestStatus BadRequest           = of(400, "Bad Request");
    public static final RestStatus Unauthorized         = of(401, "Unauthorized");
    public static final RestStatus PaymentRequired      = of(402, "Payment Required");
    public static final RestStatus Forbidden            = of(403, "Forbidden");
    public static final RestStatus NotFound             = of(404, "Not Found");
    public static final RestStatus MethodNotAllowed     = of(405, "Method Not Allowed");
    public static final RestStatus NotAcceptable        = of(406, "Not Acceptable");
    public static final RestStatus RequestTimeout       = of(408, "Request Timeout");
    public static final RestStatus Conflict             = of(409, "Conflict");
    public static final RestStatus Gone                 = of(410, "Gone");
    public static final RestStatus LengthRequired       = of(411, "Length Required");
    public static final RestStatus PreconditionFailed   = of(412, "Precondition Failed");
    public static final RestStatus PayloadTooLarge      = of(413, "Payload Too Large");
    public static final RestStatus UriTooLong           = of(414, "URI Too Long");
    public static final RestStatus UnsupportedMediaType = of(415, "Unsupported Media Type");
    public static final RestStatus UnprocessableEntity  = of(422, "Unprocessable Entity");
    public static final RestStatus Locked               = of(423, "Locked");
    public static final RestStatus TooManyRequests      = of(429, "Too Many Requests");
    public static final RestStatus UnavailableForLegalReasons = of(451, "Unavailable For Legal Reasons");

    // ----- STANDARD: 5xx Server Error --------
    public static final RestStatus InternalServerError  = of(500, "Internal Server Error");
    public static final RestStatus NotImplemented       = of(501, "Not Implemented");
    public static final RestStatus BadGateway           = of(502, "Bad Gateway");
    public static final RestStatus ServiceUnavailable   = of(503, "Service Unavailable");
    public static final RestStatus GatewayTimeout       = of(504, "Gateway Timeout");

    // ----- NON STANDARD (пропозиції) --------
    // 44x - уточнення 400 Bad Request: клієнт може зрозуміти, ЩО саме не так
    /** Відсутній обов'язковий заголовок (напр. Authorization, Content-Type) */
    public static final RestStatus HeaderRequired          = of(440, "Header Required");
    /** Заголовок присутній, але має неправильний формат */
    public static final RestStatus HeaderMalformed         = of(441, "Header Malformed");
    /** Відсутній обов'язковий параметр запиту (?login=...) */
    public static final RestStatus QueryParameterRequired  = of(442, "Query Parameter Required");
    /** Параметр запиту має неправильний формат (напр. page=abc) */
    public static final RestStatus QueryParameterMalformed = of(443, "Query Parameter Malformed");
    /** Ресурс вимагає токен, а його не передано */
    public static final RestStatus TokenRequired           = of(444, "Token Required");
    /** Токен прострочений - клієнту слід оновити його (refresh) */
    public static final RestStatus TokenExpired            = of(445, "Token Expired");
    /** Токен пошкоджений / підпис не збігається */
    public static final RestStatus TokenMalformed          = of(446, "Token Malformed");
    /** Дані форми не пройшли валідацію; data - помилки по полях */
    public static final RestStatus ValidationFailed        = of(447, "Validation Failed");
    /** Логін / email вже зайнятий іншим користувачем */
    public static final RestStatus LoginOccupied           = of(448, "Login Occupied");
    /** Обліковий запис заблоковано (багато невдалих входів, бан) */
    public static final RestStatus AccountLocked           = of(449, "Account Locked");
    // 55x - уточнення 500: де саме сталась серверна помилка
    /** Помилка СУБД (підключення, SQL) */
    public static final RestStatus DatabaseError              = of(550, "Database Error");
    /** Недоступний зовнішній сервіс (пошта, платіжна система, API) */
    public static final RestStatus ExternalServiceUnavailable = of(551, "External Service Unavailable");
    /** Помилка конфігурації сервера (відсутні налаштування, ключі) */
    public static final RestStatus ConfigurationError         = of(552, "Configuration Error");

    /** Усі статичні статуси у порядку оголошення */
    public static List<RestStatus> values() {
        return Collections.unmodifiableList(new ArrayList<>(REGISTRY.values()));
    }

    /** Статус за кодом або null, якщо такого коду в переліку немає */
    public static RestStatus fromCode(int code) {
        return REGISTRY.get(code);
    }

    /** Чи є код стандартним HTTP (не власним розширенням проєкту) */
    public boolean isStandard() {
        return !(code >= 440 && code <= 449) && !(code >= 550 && code <= 559);
    }

    /** Копія статусу з іншим повідомленням (для незмінних статичних полів) */
    public RestStatus withMessage(String message) {
        return new RestStatus(isOk, code, message);
    }

    private void checkNotFrozen() {
        if (frozen) {
            throw new UnsupportedOperationException(
                    "Predefined status " + code + " is immutable; use withMessage() or new RestStatus()");
        }
    }

    public boolean isOk() {
        return isOk;
    }

    public RestStatus setOk(boolean isOk) {
        checkNotFrozen();
        this.isOk = isOk;
        return this;
    }

    public int getCode() {
        return code;
    }

    public RestStatus setCode(int code) {
        checkNotFrozen();
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public RestStatus setMessage(String message) {
        checkNotFrozen();
        this.message = message;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RestStatus that = (RestStatus) o;
        return isOk == that.isOk && code == that.code && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isOk, code, message);
    }

    @Override
    public String toString() {
        return "RestStatus{" +
                "isOk=" + isOk +
                ", code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
