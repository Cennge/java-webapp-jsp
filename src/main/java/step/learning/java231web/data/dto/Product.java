package step.learning.java231web.data.dto;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Товар. Належить розділу (sectionId), може мати версії
 * (колір, обсяг пам'яті тощо) зі своїми ціною та залишком
 * @author ilyag
 */
public class Product {
    private String id;
    private String sectionId;
    private String title;
    private String description;
    private String slug;
    private String imageUrl;
    private BigDecimal price = BigDecimal.ZERO;
    private int stock;
    private int orderInPrice = 10000;
    private Date deletedAt;
    private List<ProductVersion> versions = new ArrayList<>();

    public static Product fromResultSet(ResultSet rs) throws SQLException {
        Product product = new Product()
                .setId(rs.getString("product_id"))
                .setSectionId(rs.getString("product_section_id"))
                .setTitle(rs.getString("product_title"))
                .setDescription(rs.getString("product_description"))
                .setSlug(rs.getString("product_slug"))
                .setImageUrl(rs.getString("product_image"))
                .setPrice(rs.getBigDecimal("product_price"))
                .setStock(rs.getInt("product_stock"))
                .setOrderInPrice(rs.getInt("product_order"));
        Timestamp ts = rs.getTimestamp("product_delete_dt");
        if (ts != null) {
            product.setDeletedAt(new Date(ts.getTime()));
        }
        return product;
    }

    public String getId() {
        return id;
    }

    public Product setId(String id) {
        this.id = id;
        return this;
    }

    public String getSectionId() {
        return sectionId;
    }

    public Product setSectionId(String sectionId) {
        this.sectionId = sectionId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Product setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Product setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getSlug() {
        return slug;
    }

    public Product setSlug(String slug) {
        this.slug = slug;
        return this;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Product setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Product setPrice(BigDecimal price) {
        this.price = price != null ? price : BigDecimal.ZERO;
        return this;
    }

    public int getStock() {
        return stock;
    }

    public Product setStock(int stock) {
        this.stock = stock;
        return this;
    }

    public int getOrderInPrice() {
        return orderInPrice;
    }

    public Product setOrderInPrice(int orderInPrice) {
        this.orderInPrice = orderInPrice;
        return this;
    }

    public Date getDeletedAt() {
        return deletedAt;
    }

    public Product setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
        return this;
    }

    public List<ProductVersion> getVersions() {
        return versions;
    }

    public Product setVersions(List<ProductVersion> versions) {
        this.versions = versions != null ? versions : new ArrayList<>();
        return this;
    }
}
