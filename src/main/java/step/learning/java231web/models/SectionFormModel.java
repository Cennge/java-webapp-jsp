package step.learning.java231web.models;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.fileupload2.core.FileItem;
import step.learning.java231web.services.form.FormParseResult;

/**
 * Модель форми додавання розділу товарів (адмінка /admin/section).
 * Поля форми: section-parent-id (опціонально), section-title, section-description,
 * section-slug, section-order, section-image (файл)
 * @author ilyag
 */
public class SectionFormModel {
    private String parentId;
    private String title;
    private String description;
    private String slug;
    private String order;
    private FileItem<?> imageItem;

    public static SectionFormModel fromForm(FormParseResult form) {
        Map<String, String> f = form.getFields();
        return new SectionFormModel()
                .setParentId(f.get("section-parent-id"))
                .setTitle(f.get("section-title"))
                .setDescription(f.get("section-description"))
                .setSlug(f.get("section-slug"))
                .setOrder(f.get("section-order"))
                .setImageItem(form.getFiles().get("section-image"));
    }

    /** Помилки формату по полях (наявність батька та унікальність слагу - перевіряє БД) */
    public Map<String, String> getValidationErrors() {
        Map<String, String> errors = new LinkedHashMap<>();
        FormValidation.text(errors, "section-title", title, "Назва розділу", 2, 128);
        FormValidation.text(errors, "section-description", description, "Опис розділу", 5, 2000);
        FormValidation.slug(errors, "section-slug", slug, "smart-gadgets");
        FormValidation.nonNegativeInt(errors, "section-order", order, "Порядок відображення");
        FormValidation.image(errors, "section-image", imageItem, true);
        return Collections.unmodifiableMap(errors);
    }

    public boolean isValid() {
        return getValidationErrors().isEmpty();
    }

    public String getParentId() {
        return parentId;
    }

    public SectionFormModel setParentId(String parentId) {
        this.parentId = FormValidation.trimOrNull(parentId);
        return this;
    }

    public String getTitle() {
        return title;
    }

    public SectionFormModel setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public SectionFormModel setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getSlug() {
        return slug;
    }

    public SectionFormModel setSlug(String slug) {
        this.slug = slug;
        return this;
    }

    public String getOrder() {
        return order;
    }

    public SectionFormModel setOrder(String order) {
        this.order = order;
        return this;
    }

    public FileItem<?> getImageItem() {
        return imageItem;
    }

    public SectionFormModel setImageItem(FileItem<?> imageItem) {
        this.imageItem = imageItem;
        return this;
    }
}
