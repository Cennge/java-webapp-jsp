package step.learning.java231web.services.storage;

import com.google.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.regex.Pattern;
import org.apache.commons.fileupload2.core.FileItem;

/**
 * Сховище файлів на диску.
 * Каталог: системна властивість "java-webapp.storage", за замовчуванням
 * {user.home}/java-webapp/storage (каталог поза веб-застосунком, щоб
 * файли не зникали при перепублікації)
 * @author ilyag
 */
@Singleton
public class DiskStorageService implements IStorageService {
    // імена, які видає save(): UUID + дозволене розширення. Інші імена при читанні
    // відхиляються - захист від обходу каталогів (../../etc)
    private static final Pattern SAFE_NAME = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9_-]*\\.[A-Za-z0-9]{1,5}$");

    private final Path root;

    public DiskStorageService() {
        this(Paths.get(System.getProperty("java-webapp.storage",
                System.getProperty("user.home") + "/java-webapp/storage")));
    }

    public DiskStorageService(Path root) {
        this.root = root.toAbsolutePath().normalize();
    }

    public Path getRoot() {
        return root;
    }

    @Override
    public String save(FileItem<?> item) throws IOException {
        String ext = IStorageService.extensionOf(item.getName());
        if (ext == null) {
            throw new IOException("File not allowed without extension");
        }
        if (!IStorageService.ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IOException("File extension '" + ext + "' not allowed");
        }
        Files.createDirectories(root);
        Path file;
        String savedName;
        do {
            savedName = UUID.randomUUID() + ext;
            file = root.resolve(savedName);
        } while (Files.exists(file));
        item.write(file);
        return savedName;
    }

    @Override
    public InputStream getStream(String fileName) throws IOException {
        if (fileName == null || !SAFE_NAME.matcher(fileName).matches()) {
            throw new IOException("Invalid file name");
        }
        Path file = root.resolve(fileName).normalize();
        if (!file.startsWith(root) || !Files.isRegularFile(file)) {
            throw new IOException("File not found: " + fileName);
        }
        return Files.newInputStream(file);
    }
}
