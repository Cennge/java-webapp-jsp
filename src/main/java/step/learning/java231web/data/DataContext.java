package step.learning.java231web.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import step.learning.java231web.data.dao.ProductDao;
import step.learning.java231web.data.dao.ProductSectionDao;
import step.learning.java231web.data.dao.ProductVersionDao;
import step.learning.java231web.data.dto.Product;
import step.learning.java231web.data.dto.ProductSection;
import step.learning.java231web.data.dto.ProductVersion;

/**
 * Контекст даних каталогу (аналог DbContext): доступ до DAO розділів,
 * товарів і версій та одноразова установка - створення таблиць і
 * наповнення демо-даними, якщо каталог порожній
 * @author ilyag
 */
@Singleton
public class DataContext {
    private static final Logger LOGGER = Logger.getLogger(DataContext.class.getName());

    private final ProductSectionDao sections;
    private final ProductDao products;
    private final ProductVersionDao versions;
    private volatile boolean installed = false;

    @Inject
    public DataContext(ProductSectionDao sections, ProductDao products, ProductVersionDao versions) {
        this.sections = sections;
        this.products = products;
        this.versions = versions;
    }

    public ProductSectionDao sections() {
        return sections;
    }

    public ProductDao products() {
        return products;
    }

    public ProductVersionDao versions() {
        return versions;
    }

    /**
     * Створює таблиці та демо-дані при першому зверненні.
     * Якщо БД недоступна - виняток, наступне звернення спробує знову
     */
    public void ensureInstalled() throws SQLException {
        if (installed) {
            return;
        }
        synchronized (this) {
            if (installed) {
                return;
            }
            sections.createTable();
            products.createTable();
            versions.createTable();
            if (sections.count() == 0) {
                seed();
            }
            installed = true;
        }
    }

    private void seed() throws SQLException {
        for (ProductSection section : loadSeedSections()) {
            // зображення з sections.json у нашому сховищі відсутні - не посилаємось на них
            sections.add(section.setImageUrl(null));
        }
        ProductSection tv = sections.getBySlug("tv");
        if (tv != null) {
            products.add(new Product()
                    .setSectionId(tv.getId())
                    .setTitle("Samsung Smart 4K UHD TV 55\"")
                    .setDescription("Телевізор Samsung Crystal UHD 4K")
                    .setSlug("samsung-smart-4k-55")
                    .setPrice(new BigDecimal("18999.00"))
                    .setStock(10)
                    .setOrderInPrice(1));
            products.add(new Product()
                    .setSectionId(tv.getId())
                    .setTitle("LG OLED 65\" Cinema Series")
                    .setDescription("Телевізор LG OLED з підтримкою HDR10")
                    .setSlug("lg-oled-65")
                    .setPrice(new BigDecimal("45999.00"))
                    .setStock(5)
                    .setOrderInPrice(2));
        }
        ProductSection android = sections.getBySlug("android");
        if (android != null) {
            Product iphone = new Product()
                    .setSectionId(android.getId())
                    .setTitle("iPhone 18 Pro Max")
                    .setDescription("Найкращий iPhone для вас і вашої родини")
                    .setSlug("iphone-18-pro-max")
                    .setPrice(new BigDecimal("125000.00"))
                    .setStock(100)
                    .setOrderInPrice(1);
            products.add(iphone);
            versions.add(new ProductVersion().setProductId(iphone.getId())
                    .setPrice(new BigDecimal("125000.00")).setStock(100).setOrderInPrice(1));
            versions.add(new ProductVersion().setProductId(iphone.getId())
                    .setTitleAddon("Black").setDescriptionAddon("Колір: чорний").setSlugAddon("black")
                    .setPrice(new BigDecimal("126000.00")).setStock(50).setOrderInPrice(2));
            versions.add(new ProductVersion().setProductId(iphone.getId())
                    .setTitleAddon("Glacier Blue").setDescriptionAddon("Колір: льодовиково-блакитний")
                    .setSlugAddon("glacier-blue")
                    .setPrice(new BigDecimal("130000.00")).setStock(10).setOrderInPrice(3));
        }
        LOGGER.info("Catalog seeded with demo sections and products");
    }

    private List<ProductSection> loadSeedSections() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("sections.json")) {
            if (in == null) {
                return List.of();
            }
            List<ProductSection> list = new Gson().fromJson(
                    new InputStreamReader(in, StandardCharsets.UTF_8),
                    new TypeToken<List<ProductSection>>() {}.getType());
            if (list == null) {
                return List.of();
            }
            // батьківські розділи - першими (FK у схемі немає, але порядок наочніший)
            list.sort((a, b) -> Boolean.compare(a.getParentId() != null, b.getParentId() != null));
            return list;
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Could not read sections.json", ex);
            return List.of();
        }
    }
}
