package step.learning.java231web.services.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import step.learning.java231web.TestFiles;

import static org.junit.jupiter.api.Assertions.*;

public class DiskStorageServiceTest {

    @TempDir
    Path root;

    @Test
    public void testSaveAndReadBack() throws IOException {
        DiskStorageService storage = new DiskStorageService(root.resolve("storage"));
        byte[] content = { 1, 2, 3, 4 };
        String saved = storage.save(TestFiles.file("Фото товару.PNG", content));

        assertTrue(saved.matches("[0-9a-f-]{36}\\.png"), "нове ім'я: UUID + розширення: " + saved);
        try (InputStream in = storage.getStream(saved)) {
            assertArrayEquals(content, in.readAllBytes());
        }
    }

    @Test
    public void testRejectsNotAllowedExtensions() {
        DiskStorageService storage = new DiskStorageService(root);
        assertThrows(IOException.class, () -> storage.save(TestFiles.file("shell.jsp", new byte[] { 1 })));
        assertThrows(IOException.class, () -> storage.save(TestFiles.file("noext", new byte[] { 1 })));
    }

    @Test
    public void testPathTraversalIsRejected() throws IOException {
        Path secret = root.resolve("secret.png");
        Files.write(secret, new byte[] { 42 });
        DiskStorageService storage = new DiskStorageService(root.resolve("storage"));
        Files.createDirectories(storage.getRoot());

        assertThrows(IOException.class, () -> storage.getStream("../secret.png"));
        assertThrows(IOException.class, () -> storage.getStream("..%2Fsecret.png"));
        assertThrows(IOException.class, () -> storage.getStream("missing.png"));
        assertThrows(IOException.class, () -> storage.getStream(null));
    }

    @Test
    public void testExtensionHelpers() {
        assertEquals(".webp", IStorageService.extensionOf("a.b.WEBP"));
        assertNull(IStorageService.extensionOf("file."));
        assertNull(IStorageService.extensionOf("file"));
        assertTrue(IStorageService.isAllowed("x.jpeg"));
        assertFalse(IStorageService.isAllowed("x.svg"), "SVG може містити скрипти");
    }
}
