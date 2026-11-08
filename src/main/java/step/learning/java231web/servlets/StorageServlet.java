package step.learning.java231web.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import step.learning.java231web.services.storage.IStorageService;

/**
 * Видача файлів зі сховища: GET /storage/{fileName}
 * Імена файлів - випадкові (UUID), вміст не змінюється, тому
 * відповідь можна кешувати браузером надовго
 * @author ilyag
 */
@Singleton
public class StorageServlet extends HttpServlet {
    private final IStorageService storageService;

    @Inject
    public StorageServlet(IStorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String fileName = pathInfo == null ? "" : pathInfo.replaceFirst("^/", "");
        if (!IStorageService.isAllowed(fileName)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        InputStream in;
        try {
            in = storageService.getStream(fileName);
        } catch (IOException ex) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String mimeType = getServletContext().getMimeType(fileName);
        if (mimeType == null && fileName.toLowerCase().endsWith(".webp")) {
            mimeType = "image/webp";
        }
        // EncodingFilter встановлює UTF-8 усім відповідям; для двійкових файлів кодування
        // символів не має сенсу, тому скидаємо його (інакше "image/png;charset=UTF-8")
        resp.setCharacterEncoding((String) null);
        resp.setContentType(mimeType != null ? mimeType : "application/octet-stream");
        resp.setHeader("Cache-Control", "public, max-age=86400, immutable");
        resp.setHeader("X-Content-Type-Options", "nosniff");
        try (in; OutputStream out = resp.getOutputStream()) {
            in.transferTo(out);
        }
    }
}
