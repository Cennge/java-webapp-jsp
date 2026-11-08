package step.learning.java231web.data.dto;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Позиція кошика: товар (і, можливо, його версія), кількість та ціна
 * за одиницю на момент додавання. title / imageUrl - для відображення
 * @author ilyag
 */
public class CartItem {
    private String id;
    private String cartId;
    private String productId;
    private String productVersionId;
    private int quantity;
    private BigDecimal price = BigDecimal.ZERO;
    private Date addedAt;
    private String title;
    private String imageUrl;
    private BigDecimal total = BigDecimal.ZERO;   // ціна x кількість (полем - для JSON)

    /** Рядок з cart_items, приєднаними products та (необов'язково) product_versions */
    public static CartItem fromResultSet(ResultSet rs) throws SQLException {
        String addon = rs.getString("product_version_title_addon");
        String versionImage = rs.getString("product_version_image");
        Timestamp added = rs.getTimestamp("cart_item_add_dt");
        return new CartItem()
                .setId(rs.getString("cart_item_id"))
                .setCartId(rs.getString("cart_id"))
                .setProductId(rs.getString("product_id"))
                .setProductVersionId(rs.getString("product_version_id"))
                .setQuantity(rs.getInt("cart_item_cnt"))
                .setPrice(rs.getBigDecimal("cart_item_price"))
                .setAddedAt(added == null ? null : new Date(added.getTime()))
                .setTitle(addon == null ? rs.getString("product_title") : rs.getString("product_title") + " " + addon)
                .setImageUrl(versionImage != null ? versionImage : rs.getString("product_image"));
    }

    /** Сума позиції: ціна x кількість */
    public BigDecimal getTotal() {
        return total;
    }

    public String getId() {
        return id;
    }

    public CartItem setId(String id) {
        this.id = id;
        return this;
    }

    public String getCartId() {
        return cartId;
    }

    public CartItem setCartId(String cartId) {
        this.cartId = cartId;
        return this;
    }

    public String getProductId() {
        return productId;
    }

    public CartItem setProductId(String productId) {
        this.productId = productId;
        return this;
    }

    public String getProductVersionId() {
        return productVersionId;
    }

    public CartItem setProductVersionId(String productVersionId) {
        this.productVersionId = productVersionId;
        return this;
    }

    public int getQuantity() {
        return quantity;
    }

    public CartItem setQuantity(int quantity) {
        this.quantity = quantity;
        this.total = price.multiply(BigDecimal.valueOf(quantity));
        return this;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public CartItem setPrice(BigDecimal price) {
        this.price = price != null ? price : BigDecimal.ZERO;
        this.total = this.price.multiply(BigDecimal.valueOf(quantity));
        return this;
    }

    public Date getAddedAt() {
        return addedAt;
    }

    public CartItem setAddedAt(Date addedAt) {
        this.addedAt = addedAt;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public CartItem setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public CartItem setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }
}
