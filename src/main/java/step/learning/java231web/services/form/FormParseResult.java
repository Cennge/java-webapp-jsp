package step.learning.java231web.services.form;

import java.util.Map;
import org.apache.commons.fileupload2.core.FileItem;

/**
 * Результат розбору форми: текстові поля та файли (за іменами полів)
 * @author ilyag
 */
public interface FormParseResult {

    Map<String, String> getFields();

    Map<String, FileItem<?>> getFiles();

}
