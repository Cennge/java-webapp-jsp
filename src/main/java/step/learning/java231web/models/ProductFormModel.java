package step.learning.java231web.models;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.fileupload2.core.FileItem;
import step.learning.java231web.services.form.FormParseResult;

/**
 * Модель форми додавання товару (адмінка /admin/product).
 * Поля форми: product-section-id, product-title, product-description, product-slug,
 * product-price, product-stock, product-order, product-image (файл)
 * @author ilyag
 */
public class ProductFormModel {
    private String sectionId;
    private String title;
    private String description;
    private String slug;
    private String price;
    private String stock;
    private String order;
    private FileItem<?> imageItem;

    public static ProductFormModel fromForm(FormParseResult form) {
        Map<String, String> f = form.getFields();
        return new ProductFormModel()
                .setSectionId(f.get("product-section-id"))
                .setTitle(f.get("product-title"))
                .setDescription(f.get("product-description"))
                .setSlug(f.get("product-slug"))
                .setPrice(f.get("product-price"))
                .setStock(f.get("product-stock"))
                .setOrder(f.get("product-order"))
                .setImageItem(form.getFiles().get("product-image"));
    }

    /** Помилки формату по полях (наявність розділу та унікальність слагу - перевіряє БД) */
    public Map<String, String> getValidationErrors() {
        Map<String, String> errors = new LinkedHashMap<>();
        if (FormValidation.isBlank(sectionId)) {
            errors.put("product-section-id", "Оберіть розділ товару");
        }
        FormValidation.text(errors, "product-title", title, "Назва товару", 2, 255);
        FormValidation.text(errors, "product-description", description, "Опис товару", 5, 5000);
        FormValidation.slug(errors, "product-slug", slug, "smart-gadget");
        FormValidation.price(errors, "product-price", price);
        FormValidation.nonNegativeInt(errors, "product-stock", stock, "Кількість на складі");
        FormValidation.nonNegativeInt(errors, "product-order", order, "Порядок відображення");
        FormValidation.image(errors, "product-image", imageItem, true);
        return Collections.unmodifiableMap(errors);
    }

    public boolean isValid() {
        return getValidationErrors().isEmpty();
    }

    public String getSectionId() {
        return sectionId;
    }

    public ProductFormModel setSectionId(String sectionId) {
        this.sectionId = FormValidation.trimOrNull(sectionId);
        return this;
    }

    public String getTitle() {
        return title;
    }

    public ProductFormModel setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ProductFormModel setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getSlug() {
        return slug;
    }

    public ProductFormModel setSlug(String slug) {
        this.slug = slug;
        return this;
    }

    public String getPrice() {
        return price;
    }

    public ProductFormModel setPrice(String price) {
        this.price = price;
        return this;
    }

    public String getStock() {
        return stock;
    }

    public ProductFormModel setStock(String stock) {
        this.stock = stock;
        return this;
    }

    public String getOrder() {
        return order;
    }

    public ProductFormModel setOrder(String order) {
        this.order = order;
        return this;
    }

    public FileItem<?> getImageItem() {
        return imageItem;
    }

    public ProductFormModel setImageItem(FileItem<?> imageItem) {
        this.imageItem = imageItem;
        return this;
    }
}
