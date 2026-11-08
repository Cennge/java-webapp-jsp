package step.learning.java231web.data.dto;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Версія товару: доповнення до назви / опису / слагу основного товару
 * (iPhone 18 Pro Max + "Black" -> "iPhone 18 Pro Max Black") зі своїми
 * ціною, залишком та зображенням (якщо зображення немає - береться товарне)
 * @author ilyag
 */
public class ProductVersion {
    private String id;
    private String productId;
    private String titleAddon;
    private String descriptionAddon;
    private String slugAddon;
    private String imageUrl;
    private BigDecimal price = BigDecimal.ZERO;
    private int stock;
    private int orderInPrice = 10000;
    private Date deletedAt;

    public static ProductVersion fromResultSet(ResultSet rs) throws SQLException {
        ProductVersion version = new ProductVersion()
                .setId(rs.getString("product_version_id"))
                .setProductId(rs.getString("product_id"))
                .setTitleAddon(rs.getString("product_version_title_addon"))
                .setDescriptionAddon(rs.getString("product_version_description_addon"))
                .setSlugAddon(rs.getString("product_version_slug_addon"))
                .setImageUrl(rs.getString("product_version_image"))
                .setPrice(rs.getBigDecimal("product_version_price"))
                .setStock(rs.getInt("product_version_stock"))
                .setOrderInPrice(rs.getInt("product_version_order"));
        Timestamp ts = rs.getTimestamp("product_version_delete_dt");
        if (ts != null) {
            version.setDeletedAt(new Date(ts.getTime()));
        }
        return version;
    }

    public String getId() {
        return id;
    }

    public ProductVersion setId(String id) {
        this.id = id;
        return this;
    }

    public String getProductId() {
        return productId;
    }

    public ProductVersion setProductId(String productId) {
        this.productId = productId;
        return this;
    }

    public String getTitleAddon() {
        return titleAddon;
    }

    public ProductVersion setTitleAddon(String titleAddon) {
        this.titleAddon = titleAddon;
        return this;
    }

    public String getDescriptionAddon() {
        return descriptionAddon;
    }

    public ProductVersion setDescriptionAddon(String descriptionAddon) {
        this.descriptionAddon = descriptionAddon;
        return this;
    }

    public String getSlugAddon() {
        return slugAddon;
    }

    public ProductVersion setSlugAddon(String slugAddon) {
        this.slugAddon = slugAddon;
        return this;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public ProductVersion setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public ProductVersion setPrice(BigDecimal price) {
        this.price = price != null ? price : BigDecimal.ZERO;
        return this;
    }

    public int getStock() {
        return stock;
    }

    public ProductVersion setStock(int stock) {
        this.stock = stock;
        return this;
    }

    public int getOrderInPrice() {
        return orderInPrice;
    }

    public ProductVersion setOrderInPrice(int orderInPrice) {
        this.orderInPrice = orderInPrice;
        return this;
    }

    public Date getDeletedAt() {
        return deletedAt;
    }

    public ProductVersion setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
        return this;
    }
}
