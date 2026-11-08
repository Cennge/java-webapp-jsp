package step.learning.java231web;

import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.apache.commons.fileupload2.core.FileItem;
import step.learning.java231web.services.form.FormParseResult;

/**
 * Тестові заглушки: файл форми (FileItem) та результат розбору форми
 */
public final class TestFiles {

    private TestFiles() {
    }

    /** Файл форми з іменем та вмістом; write(path) записує вміст на диск */
    public static FileItem<?> file(String name, byte[] content) {
        return (FileItem<?>) Proxy.newProxyInstance(
                FileItem.class.getClassLoader(),
                new Class<?>[] { FileItem.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "getName" -> name;
                    case "getSize" -> (long) content.length;
                    case "get" -> content;
                    case "isFormField" -> false;
                    case "write" -> {
                        Files.write((Path) args[0], content);
                        yield proxy;
                    }
                    case "toString" -> "FileItem(" + name + ")";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(method.getName());
                });
    }

    public static FormParseResult form(Map<String, String> fields, Map<String, FileItem<?>> files) {
        return new FormParseResult() {
            @Override
            public Map<String, String> getFields() {
                return fields;
            }

            @Override
            public Map<String, FileItem<?>> getFiles() {
                return files;
            }
        };
    }
}
