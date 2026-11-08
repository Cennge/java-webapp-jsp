package step.learning.java231web.data.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import step.learning.java231web.data.dto.Product;
import step.learning.java231web.rest.RestPagination;
import step.learning.java231web.services.db.IDbService;

/**
 * Товари (таблиця products); видалення - м'яке (product_delete_dt)
 * @author ilyag
 */
@Singleton
public class ProductDao {
    private final IDbService dbService;

    @Inject
    public ProductDao(IDbService dbService) {
        this.dbService = dbService;
    }

    public void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS products ("
                + "product_id CHAR(36) PRIMARY KEY, "
                + "product_section_id CHAR(36) NOT NULL, "
                + "product_title VARCHAR(256) NOT NULL, "
                + "product_description TEXT NULL, "
                + "product_slug VARCHAR(256) NOT NULL UNIQUE, "
                + "product_price DECIMAL(12, 2) NOT NULL DEFAULT 0, "
                + "product_stock INT NOT NULL DEFAULT 0, "
                + "product_order INT NOT NULL DEFAULT 10000, "
                + "product_image VARCHAR(256) NULL, "
                + "product_delete_dt DATETIME NULL, "
                + "INDEX ix_products_section (product_section_id)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try (Statement stmt = dbService.getConnection().createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    public void add(Product product) throws SQLException {
        String sql = "INSERT INTO products "
                + "(product_id, product_section_id, product_title, product_description, product_slug, "
                + "product_price, product_stock, product_order, product_image) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        if (product.getId() == null) {
            product.setId(UUID.randomUUID().toString());
        }
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, product.getId());
            prep.setString(2, product.getSectionId());
            prep.setString(3, product.getTitle());
            prep.setString(4, product.getDescription());
            prep.setString(5, product.getSlug());
            prep.setBigDecimal(6, product.getPrice());
            prep.setInt(7, product.getStock());
            prep.setInt(8, product.getOrderInPrice());
            prep.setString(9, product.getImageUrl());
            prep.executeUpdate();
        }
    }

    public int count() throws SQLException {
        try (Statement stmt = dbService.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products WHERE product_delete_dt IS NULL")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countBySection(String sectionId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE product_section_id = ? AND product_delete_dt IS NULL";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, sectionId);
            try (ResultSet rs = prep.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Сторінка товарів розділу у порядку відображення */
    public List<Product> getBySection(String sectionId, RestPagination pagination) throws SQLException {
        String sql = "SELECT * FROM products WHERE product_section_id = ? AND product_delete_dt IS NULL "
                + "ORDER BY product_order, product_title LIMIT ? OFFSET ?";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, sectionId);
            prep.setInt(2, pagination.getPageSize());
            prep.setInt(3, pagination.getOffset());
            return readList(prep);
        }
    }

    /** Сторінка усіх товарів */
    public List<Product> getPage(RestPagination pagination) throws SQLException {
        String sql = "SELECT * FROM products WHERE product_delete_dt IS NULL "
                + "ORDER BY product_order, product_title LIMIT ? OFFSET ?";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setInt(1, pagination.getPageSize());
            prep.setInt(2, pagination.getOffset());
            return readList(prep);
        }
    }

    /** Усі товари (для переліків у формах адмінки) */
    public List<Product> getAll() throws SQLException {
        String sql = "SELECT * FROM products WHERE product_delete_dt IS NULL ORDER BY product_title";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            return readList(prep);
        }
    }

    /** Товар за id або слагом */
    public Product getByIdOrSlug(String idOrSlug) throws SQLException {
        String sql = "SELECT * FROM products "
                + "WHERE (product_id = ? OR product_slug = ?) AND product_delete_dt IS NULL LIMIT 1";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, idOrSlug);
            prep.setString(2, idOrSlug);
            try (ResultSet rs = prep.executeQuery()) {
                return rs.next() ? Product.fromResultSet(rs) : null;
            }
        }
    }

    public boolean existsById(String id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE product_id = ? AND product_delete_dt IS NULL";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, id);
            try (ResultSet rs = prep.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean isSlugAvailable(String slug) throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE product_slug = ?";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, slug);
            try (ResultSet rs = prep.executeQuery()) {
                return rs.next() && rs.getInt(1) == 0;
            }
        }
    }

    private List<Product> readList(PreparedStatement prep) throws SQLException {
        List<Product> list = new ArrayList<>();
        try (ResultSet rs = prep.executeQuery()) {
            while (rs.next()) {
                list.add(Product.fromResultSet(rs));
            }
        }
        return list;
    }
}
