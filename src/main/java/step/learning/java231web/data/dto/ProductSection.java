package step.learning.java231web.data.dto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Розділ (категорія) товарів. Розділи утворюють дерево (parentId),
 * у відповідях API - з підрозділами (children) та товарами (products)
 * @author ilyag
 */
public class ProductSection {
    private String id;
    private String parentId;
    private String title;
    private String description;
    private String slug;
    private String imageUrl;
    private int orderInPrice = 10000;
    private Date deletedAt;
    private List<Product> products = new ArrayList<>();
    private List<ProductSection> children = new ArrayList<>();

    public static ProductSection fromResultSet(ResultSet rs) throws SQLException {
        ProductSection section = new ProductSection()
                .setId(rs.getString("product_section_id"))
                .setParentId(rs.getString("product_section_parent_id"))
                .setTitle(rs.getString("product_section_title"))
                .setDescription(rs.getString("product_section_description"))
                .setSlug(rs.getString("product_section_slug"))
                .setImageUrl(rs.getString("product_section_image"))
                .setOrderInPrice(rs.getInt("product_section_order"));
        Timestamp ts = rs.getTimestamp("product_section_delete_dt");
        if (ts != null) {
            section.setDeletedAt(new Date(ts.getTime()));
        }
        return section;
    }

    public String getId() {
        return id;
    }

    public ProductSection setId(String id) {
        this.id = id;
        return this;
    }

    public String getParentId() {
        return parentId;
    }

    public ProductSection setParentId(String parentId) {
        this.parentId = parentId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public ProductSection setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ProductSection setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getSlug() {
        return slug;
    }

    public ProductSection setSlug(String slug) {
        this.slug = slug;
        return this;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public ProductSection setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public int getOrderInPrice() {
        return orderInPrice;
    }

    public ProductSection setOrderInPrice(int orderInPrice) {
        this.orderInPrice = orderInPrice;
        return this;
    }

    public Date getDeletedAt() {
        return deletedAt;
    }

    public ProductSection setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
        return this;
    }

    public List<Product> getProducts() {
        return products;
    }

    public ProductSection setProducts(List<Product> products) {
        this.products = products != null ? products : new ArrayList<>();
        return this;
    }

    public List<ProductSection> getChildren() {
        return children;
    }

    public ProductSection setChildren(List<ProductSection> children) {
        this.children = children != null ? children : new ArrayList<>();
        return this;
    }
}
