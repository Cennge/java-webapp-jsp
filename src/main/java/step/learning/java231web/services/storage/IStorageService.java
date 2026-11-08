package step.learning.java231web.services.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.commons.fileupload2.core.FileItem;

/**
 * Сховище завантажених файлів. Файли зберігаються під новими
 * випадковими іменами (UUID + розширення), у БД - лише це ім'я
 * @author ilyag
 */
public interface IStorageService {

    /** Дозволені розширення файлів (лише зображення) */
    List<String> ALLOWED_EXTENSIONS = List.of(".jpg", ".jpeg", ".png", ".webp", ".gif", ".bmp");

    /**
     * Зберігає файл
     * @return ім'я, під яким файл збережено
     * @throws IOException якщо розширення не дозволене або запис не вдався
     */
    String save(FileItem<?> item) throws IOException;

    /**
     * Потік для читання збереженого файлу
     * @throws IOException якщо ім'я некоректне або файлу немає
     */
    InputStream getStream(String fileName) throws IOException;

    /** Розширення файлу у нижньому регістрі (".png") або null, якщо його немає */
    static String extensionOf(String fileName) {
        if (fileName == null) {
            return null;
        }
        int dot = fileName.lastIndexOf('.');
        return dot < 0 || dot == fileName.length() - 1 ? null : fileName.substring(dot).toLowerCase();
    }

    static boolean isAllowed(String fileName) {
        String ext = extensionOf(fileName);
        return ext != null && ALLOWED_EXTENSIONS.contains(ext);
    }

}
