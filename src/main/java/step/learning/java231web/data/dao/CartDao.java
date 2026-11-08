package step.learning.java231web.data.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import step.learning.java231web.data.dto.Cart;
import step.learning.java231web.data.dto.CartItem;
import step.learning.java231web.services.db.IDbService;

/**
 * Кошики (carts) та їх позиції (cart_items).
 * Стан кошика: 0 - відкритий, 1 - оформлений, 2 - скасований
 * @author ilyag
 */
@Singleton
public class CartDao {
    private static final String ITEMS_SELECT = "SELECT ci.*, p.product_title, p.product_image, "
            + "v.product_version_title_addon, v.product_version_image "
            + "FROM cart_items ci "
            + "JOIN products p ON p.product_id = ci.product_id "
            + "LEFT JOIN product_versions v ON v.product_version_id = ci.product_version_id ";

    private final IDbService dbService;

    @Inject
    public CartDao(IDbService dbService) {
        this.dbService = dbService;
    }

    public void createTable() throws SQLException {
        try (Statement stmt = dbService.getConnection().createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS carts ("
                    + "cart_id CHAR(36) PRIMARY KEY, "
                    + "user_id CHAR(36) NOT NULL, "
                    + "cart_status TINYINT NOT NULL DEFAULT 0, "
                    + "cart_price DECIMAL(12, 2) NOT NULL DEFAULT 0, "
                    + "cart_open_dt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "cart_close_dt DATETIME NULL, "
                    + "INDEX ix_carts_user (user_id, cart_status)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS cart_items ("
                    + "cart_item_id CHAR(36) PRIMARY KEY, "
                    + "cart_id CHAR(36) NOT NULL, "
                    + "product_id CHAR(36) NOT NULL, "
                    + "product_version_id CHAR(36) NULL, "
                    + "cart_item_cnt INT NOT NULL DEFAULT 1, "
                    + "cart_item_price DECIMAL(12, 2) NOT NULL DEFAULT 0, "
                    + "cart_item_add_dt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "INDEX ix_cart_items_cart (cart_id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        }
    }

    /** Відкритий кошик користувача з позиціями або null */
    public Cart getOpenCart(String userId) throws SQLException {
        String sql = "SELECT * FROM carts WHERE user_id = ? AND cart_status = " + Cart.OPEN
                + " ORDER BY cart_open_dt DESC LIMIT 1";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, userId);
            try (ResultSet rs = prep.executeQuery()) {
                return rs.next() ? withItems(Cart.fromResultSet(rs)) : null;
            }
        }
    }

    public Cart getById(String cartId) throws SQLException {
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(
                "SELECT * FROM carts WHERE cart_id = ?")) {
            prep.setString(1, cartId);
            try (ResultSet rs = prep.executeQuery()) {
                return rs.next() ? withItems(Cart.fromResultSet(rs)) : null;
            }
        }
    }

    /** Закриті кошики користувача (status - ORDERED / DROPPED, null - усі закриті), нові першими */
    public List<Cart> getClosedCarts(String userId, Integer status) throws SQLException {
        String sql = "SELECT * FROM carts WHERE user_id = ? AND "
                + (status == null ? "cart_status <> " + Cart.OPEN : "cart_status = " + status)
                + " ORDER BY cart_close_dt DESC";
        List<Cart> list = new ArrayList<>();
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, userId);
            try (ResultSet rs = prep.executeQuery()) {
                while (rs.next()) {
                    list.add(Cart.fromResultSet(rs));
                }
            }
        }
        for (Cart cart : list) {
            withItems(cart);
        }
        return list;
    }

    public CartItem getItemById(String cartItemId) throws SQLException {
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(
                ITEMS_SELECT + "WHERE ci.cart_item_id = ?")) {
            prep.setString(1, cartItemId);
            try (ResultSet rs = prep.executeQuery()) {
                return rs.next() ? CartItem.fromResultSet(rs) : null;
            }
        }
    }

    /** Відкритий кошик користувача; якщо його немає - створюється новий */
    public Cart getOrCreateOpenCart(String userId) throws SQLException {
        Cart cart = getOpenCart(userId);
        if (cart != null) {
            return cart;
        }
        String id = UUID.randomUUID().toString();
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(
                "INSERT INTO carts (cart_id, user_id, cart_status) VALUES (?, ?, " + Cart.OPEN + ")")) {
            prep.setString(1, id);
            prep.setString(2, userId);
            prep.executeUpdate();
        }
        return getById(id);
    }

    /** Чи є вже у кошику цей товар (з цією версією) */
    public CartItem findItem(Cart cart, String productId, String productVersionId) {
        for (CartItem item : cart.getItems()) {
            if (item.getProductId().equals(productId)
                    && java.util.Objects.equals(item.getProductVersionId(), productVersionId)) {
                return item;
            }
        }
        return null;
    }

    public void addItem(String cartId, String productId, String productVersionId, BigDecimal price) throws SQLException {
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(
                "INSERT INTO cart_items (cart_item_id, cart_id, product_id, product_version_id, cart_item_cnt, cart_item_price) "
                + "VALUES (?, ?, ?, ?, 1, ?)")) {
            prep.setString(1, UUID.randomUUID().toString());
            prep.setString(2, cartId);
            prep.setString(3, productId);
            prep.setString(4, productVersionId);
            prep.setBigDecimal(5, price);
            prep.executeUpdate();
        }
    }

    public void setItemQuantity(String cartItemId, int quantity) throws SQLException {
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(
                "UPDATE cart_items SET cart_item_cnt = ? WHERE cart_item_id = ?")) {
            prep.setInt(1, quantity);
            prep.setString(2, cartItemId);
            prep.executeUpdate();
        }
    }

    public void deleteItem(String cartItemId) throws SQLException {
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(
                "DELETE FROM cart_items WHERE cart_item_id = ?")) {
            prep.setString(1, cartItemId);
            prep.executeUpdate();
        }
    }

    /**
     * Закриття кошика (оформлення / скасування) з фіксацією суми.
     * При оформленні зменшуються залишки товарів - в одній транзакції
     */
    public void close(Cart cart, int status) throws SQLException {
        Connection connection = dbService.getConnection();
        boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            try (PreparedStatement prep = connection.prepareStatement(
                    "UPDATE carts SET cart_status = ?, cart_price = ?, cart_close_dt = CURRENT_TIMESTAMP "
                    + "WHERE cart_id = ? AND cart_status = " + Cart.OPEN)) {
                prep.setInt(1, status);
                prep.setBigDecimal(2, cart.getTotal());
                prep.setString(3, cart.getId());
                if (prep.executeUpdate() != 1) {
                    throw new SQLException("Cart " + cart.getId() + " is not open");
                }
            }
            if (status == Cart.ORDERED) {
                for (CartItem item : cart.getItems()) {
                    String sql = item.getProductVersionId() != null
                            ? "UPDATE product_versions SET product_version_stock = product_version_stock - ? WHERE product_version_id = ?"
                            : "UPDATE products SET product_stock = product_stock - ? WHERE product_id = ?";
                    try (PreparedStatement prep = connection.prepareStatement(sql)) {
                        prep.setInt(1, item.getQuantity());
                        prep.setString(2, item.getProductVersionId() != null ? item.getProductVersionId() : item.getProductId());
                        prep.executeUpdate();
                    }
                }
            }
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(autoCommit);
        }
    }

    private Cart withItems(Cart cart) throws SQLException {
        List<CartItem> items = new ArrayList<>();
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(
                ITEMS_SELECT + "WHERE ci.cart_id = ? ORDER BY ci.cart_item_add_dt, ci.cart_item_id")) {
            prep.setString(1, cart.getId());
            try (ResultSet rs = prep.executeQuery()) {
                while (rs.next()) {
                    items.add(CartItem.fromResultSet(rs));
                }
            }
        }
        return cart.setItems(items);
    }
}
