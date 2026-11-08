package step.learning.java231web.services.form;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Розбір даних форми: звичайної (application/x-www-form-urlencoded)
 * та змішаної з файлами (multipart/form-data)
 * @author ilyag
 */
public interface IFormParseService {

    /**
     * @throws FormParseException якщо форму не вдалося розібрати (напр. файл завеликий)
     */
    FormParseResult parse(HttpServletRequest request) throws FormParseException;

    class FormParseException extends Exception {
        public FormParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
