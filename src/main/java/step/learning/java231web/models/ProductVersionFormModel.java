package step.learning.java231web.models;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.fileupload2.core.FileItem;
import step.learning.java231web.services.form.FormParseResult;

/**
 * Модель форми додавання версії товару (адмінка /admin/product-version).
 * Поля форми: product-id, version-title (доповнення до назви), version-description,
 * version-slug (доповнення до слагу), version-price, version-stock, version-order,
 * version-image (файл, опціонально - без нього береться зображення товару)
 * @author ilyag
 */
public class ProductVersionFormModel {
    private String productId;
    private String titleAddon;
    private String descriptionAddon;
    private String slugAddon;
    private String price;
    private String stock;
    private String order;
    private FileItem<?> imageItem;

    public static ProductVersionFormModel fromForm(FormParseResult form) {
        Map<String, String> f = form.getFields();
        return new ProductVersionFormModel()
                .setProductId(f.get("product-id"))
                .setTitleAddon(f.get("version-title"))
                .setDescriptionAddon(f.get("version-description"))
                .setSlugAddon(f.get("version-slug"))
                .setPrice(f.get("version-price"))
                .setStock(f.get("version-stock"))
                .setOrder(f.get("version-order"))
                .setImageItem(form.getFiles().get("version-image"));
    }

    /** Помилки формату по полях (наявність товару та унікальність слагу - перевіряє БД) */
    public Map<String, String> getValidationErrors() {
        Map<String, String> errors = new LinkedHashMap<>();
        if (FormValidation.isBlank(productId)) {
            errors.put("product-id", "Оберіть товар, для якого додається версія");
        }
        FormValidation.text(errors, "version-title", titleAddon, "Доповнення до назви", 1, 255);
        FormValidation.text(errors, "version-description", descriptionAddon, "Доповнення до опису", 3, 2000);
        FormValidation.slug(errors, "version-slug", slugAddon, "cosmic-red");
        FormValidation.price(errors, "version-price", price);
        FormValidation.nonNegativeInt(errors, "version-stock", stock, "Кількість на складі");
        FormValidation.nonNegativeInt(errors, "version-order", order, "Порядок у прайсі");
        FormValidation.image(errors, "version-image", imageItem, false);
        return Collections.unmodifiableMap(errors);
    }

    public boolean isValid() {
        return getValidationErrors().isEmpty();
    }

    public String getProductId() {
        return productId;
    }

    public ProductVersionFormModel setProductId(String productId) {
        this.productId = FormValidation.trimOrNull(productId);
        return this;
    }

    public String getTitleAddon() {
        return titleAddon;
    }

    public ProductVersionFormModel setTitleAddon(String titleAddon) {
        this.titleAddon = titleAddon;
        return this;
    }

    public String getDescriptionAddon() {
        return descriptionAddon;
    }

    public ProductVersionFormModel setDescriptionAddon(String descriptionAddon) {
        this.descriptionAddon = descriptionAddon;
        return this;
    }

    public String getSlugAddon() {
        return slugAddon;
    }

    public ProductVersionFormModel setSlugAddon(String slugAddon) {
        this.slugAddon = slugAddon;
        return this;
    }

    public String getPrice() {
        return price;
    }

    public ProductVersionFormModel setPrice(String price) {
        this.price = price;
        return this;
    }

    public String getStock() {
        return stock;
    }

    public ProductVersionFormModel setStock(String stock) {
        this.stock = stock;
        return this;
    }

    public String getOrder() {
        return order;
    }

    public ProductVersionFormModel setOrder(String order) {
        this.order = order;
        return this;
    }

    public FileItem<?> getImageItem() {
        return imageItem;
    }

    public ProductVersionFormModel setImageItem(FileItem<?> imageItem) {
        this.imageItem = imageItem;
        return this;
    }
}
