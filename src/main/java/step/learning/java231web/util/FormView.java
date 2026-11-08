package step.learning.java231web.util;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Стан форми для представлення: введені значення та помилки валідації,
 * які сервлет повернув разом з формою (атрибути formType, formFields, validationErrors).
 * Значення та помилки беруться лише для "своєї" форми (formType), щоб на сторінці
 * з кількома формами помилки товару не з'являлися у формі версії
 * @author ilyag
 */
public class FormView {
    private final Map<String, String> fields;
    private final Map<String, String> errors;

    private FormView(Map<String, String> fields, Map<String, String> errors) {
        this.fields = fields;
        this.errors = errors;
    }

    @SuppressWarnings("unchecked")
    public static FormView of(HttpServletRequest request, String formType) {
        boolean own = formType.equals(request.getAttribute("formType"));
        Map<String, String> fields = own ? (Map<String, String>) request.getAttribute("formFields") : null;
        Map<String, String> errors = own ? (Map<String, String>) request.getAttribute("validationErrors") : null;
        return new FormView(fields != null ? fields : Map.of(), errors != null ? errors : Map.of());
    }

    /** Введене значення поля (екрановане) або порожній рядок */
    public String value(String name) {
        return Html.esc(fields.get(name));
    }

    /** Чи обрано значення у select (для атрибута selected) */
    public boolean selected(String name, String optionValue) {
        return optionValue != null && optionValue.equals(fields.get(name));
    }

    /** " invalid" для групи поля з помилкою */
    public String invalid(String name) {
        return errors.containsKey(name) ? " invalid" : "";
    }

    /** Блок з текстом помилки поля або порожній рядок */
    public String error(String name) {
        String message = errors.get(name);
        return message == null ? "" : "<div class=\"field-error\">" + Html.esc(message) + "</div>";
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
