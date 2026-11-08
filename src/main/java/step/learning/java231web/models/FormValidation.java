package step.learning.java231web.models;

import java.math.BigDecimal;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.fileupload2.core.FileItem;
import step.learning.java231web.services.storage.IStorageService;

/**
 * Спільні правила валідації полів форм адмінки.
 * Кожен метод додає помилку до errors під ім'ям поля (якщо значення некоректне)
 * @author ilyag
 */
public final class FormValidation {
    public static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    private FormValidation() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static void text(Map<String, String> errors, String field, String value,
                            String label, int minLength, int maxLength) {
        if (isBlank(value)) {
            errors.put(field, "Поле «" + label + "» не може бути порожнім");
        } else if (value.trim().length() < minLength) {
            errors.put(field, "Поле «" + label + "» повинно містити щонайменше " + symbols(minLength));
        } else if (value.trim().length() > maxLength) {
            errors.put(field, "Поле «" + label + "» не може перевищувати " + symbols(maxLength));
        }
    }

    /** 1 символ, 2 символи, 5 символів, 21 символ */
    static String symbols(int n) {
        int mod10 = n % 10, mod100 = n % 100;
        String word = mod10 == 1 && mod100 != 11 ? "символ"
                : mod10 >= 2 && mod10 <= 4 && (mod100 < 12 || mod100 > 14) ? "символи"
                : "символів";
        return n + " " + word;
    }

    public static void slug(Map<String, String> errors, String field, String value, String example) {
        if (isBlank(value)) {
            errors.put(field, "Слаг не може бути порожнім");
        } else if (!SLUG_PATTERN.matcher(value.trim()).matches()) {
            errors.put(field, "Слаг може містити лише малі латинські літери, цифри та дефіси (наприклад: "
                    + example + ")");
        }
    }

    public static void nonNegativeInt(Map<String, String> errors, String field, String value, String label) {
        if (isBlank(value)) {
            errors.put(field, "Поле «" + label + "» не може бути порожнім");
            return;
        }
        try {
            if (Integer.parseInt(value.trim()) < 0) {
                errors.put(field, "Значення поля «" + label + "» не може бути від'ємним");
            }
        } catch (NumberFormatException ex) {
            errors.put(field, "Поле «" + label + "» повинно бути цілим числом");
        }
    }

    public static void price(Map<String, String> errors, String field, String value) {
        if (isBlank(value)) {
            errors.put(field, "Ціна не може бути порожньою");
            return;
        }
        BigDecimal price = parsePrice(value);
        if (price == null) {
            errors.put(field, "Некоректний формат ціни (очікується число, наприклад: 15999.00)");
        } else if (price.signum() <= 0) {
            errors.put(field, "Ціна повинна бути більшою за 0");
        } else if (price.scale() > 2) {
            errors.put(field, "Ціна може містити не більше 2 знаків після коми");
        }
    }

    /** Ціна з рядка: "15999", "15999.50" або "15999,50"; null - якщо не число */
    public static BigDecimal parsePrice(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return new BigDecimal(value.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static void image(Map<String, String> errors, String field, FileItem<?> item, boolean required) {
        if (item == null) {
            if (required) {
                errors.put(field, "Необхідно обрати файл зображення");
            }
            return;
        }
        if (IStorageService.extensionOf(item.getName()) == null) {
            errors.put(field, "Файл зображення повинен мати розширення (.jpg, .png тощо)");
        } else if (!IStorageService.isAllowed(item.getName())) {
            errors.put(field, "Непідтримуваний формат файлу. Дозволені: "
                    + String.join(", ", IStorageService.ALLOWED_EXTENSIONS));
        }
    }

    public static String trimOrNull(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
