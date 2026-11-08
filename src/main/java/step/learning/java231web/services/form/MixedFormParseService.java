package step.learning.java231web.services.form;

import com.google.inject.Singleton;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.core.FileUploadByteCountLimitException;
import org.apache.commons.fileupload2.core.FileUploadSizeException;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletDiskFileUpload;

/**
 * Розбір змішаних форм: multipart/form-data (поля + файли) засобами
 * Apache Commons FileUpload, інші форми - стандартними параметрами запиту.
 * Порожні файлові поля (файл не обрано) у результат не потрапляють
 * @author ilyag
 */
@Singleton
public class MixedFormParseService implements IFormParseService {
    private static final int MEMORY_THRESHOLD = 1024 * 1024;           // до 1 МБ - у пам'яті, більше - тимчасовий файл
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;        // 10 МБ на один файл
    private static final long MAX_REQUEST_SIZE = 20 * 1024 * 1024;     // 20 МБ на весь запит

    @Override
    public FormParseResult parse(HttpServletRequest request) throws FormParseException {
        Map<String, String> fields = new LinkedHashMap<>();
        Map<String, FileItem<?>> files = new LinkedHashMap<>();

        if (JakartaServletDiskFileUpload.isMultipartContent(request)) {
            DiskFileItemFactory factory = DiskFileItemFactory.builder()
                    .setBufferSize(MEMORY_THRESHOLD)
                    .get();
            JakartaServletDiskFileUpload upload = new JakartaServletDiskFileUpload(factory);
            upload.setFileSizeMax(MAX_FILE_SIZE);
            upload.setSizeMax(MAX_REQUEST_SIZE);
            upload.setHeaderCharset(StandardCharsets.UTF_8);   // кирилиця в іменах файлів
            try {
                List<DiskFileItem> items = upload.parseRequest(request);
                for (FileItem<?> item : items) {
                    if (item.isFormField()) {
                        fields.put(item.getFieldName(), item.getString(StandardCharsets.UTF_8));
                    } else if (item.getName() != null && !item.getName().isBlank() && item.getSize() > 0) {
                        files.put(item.getFieldName(), item);
                    }
                }
            } catch (FileUploadSizeException ex) {
                // Розбір зупиняється до кінця тіла запиту. Непрочитаний залишок понад
                // maxSwallowSize (2 МБ) Tomcat не дочитує і розриває з'єднання - браузер
                // показав би ERR_CONNECTION_RESET замість повідомлення. Тому дочитуємо залишок
                drainBody(request);
                throw new FormParseException(ex instanceof FileUploadByteCountLimitException
                        ? "Файл перевищує дозволений розмір (" + MAX_FILE_SIZE / (1024 * 1024) + " МБ)"
                        : "Розмір форми перевищує дозволений (" + MAX_REQUEST_SIZE / (1024 * 1024) + " МБ)", ex);
            } catch (java.io.IOException ex) {
                throw new FormParseException("Не вдалося обробити форму", ex);
            }
        } else {
            request.getParameterMap().forEach((name, values) ->
                    fields.put(name, values != null && values.length > 0 ? values[0] : ""));
        }

        return result(fields, files);
    }

    private static void drainBody(HttpServletRequest request) {
        try {
            request.getInputStream().transferTo(java.io.OutputStream.nullOutputStream());
        } catch (java.io.IOException | IllegalStateException ignored) {
            // клієнт міг розірвати з'єднання сам - відповідь однаково буде з помилкою
        }
    }

    private static FormParseResult result(Map<String, String> fields, Map<String, FileItem<?>> files) {
        Map<String, String> fieldsView = Collections.unmodifiableMap(fields);
        Map<String, FileItem<?>> filesView = Collections.unmodifiableMap(files);
        return new FormParseResult() {
            @Override
            public Map<String, String> getFields() {
                return fieldsView;
            }

            @Override
            public Map<String, FileItem<?>> getFiles() {
                return filesView;
            }
        };
    }
}
