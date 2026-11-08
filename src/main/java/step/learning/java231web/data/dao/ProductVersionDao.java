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
import step.learning.java231web.data.dto.ProductVersion;
import step.learning.java231web.services.db.IDbService;

/**
 * Версії товарів (таблиця product_versions). Слаг-доповнення унікальний
 * у межах товару: iphone-18-pro-max + black, iphone-18-pro-max + glacier-blue
 * @author ilyag
 */
@Singleton
public class ProductVersionDao {
    private final IDbService dbService;

    @Inject
    public ProductVersionDao(IDbService dbService) {
        this.dbService = dbService;
    }

    public void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS product_versions ("
                + "product_version_id CHAR(36) PRIMARY KEY, "
                + "product_id CHAR(36) NOT NULL, "
                + "product_version_title_addon VARCHAR(256) NULL, "
                + "product_version_description_addon TEXT NULL, "
                + "product_version_slug_addon VARCHAR(256) NULL, "
                + "product_version_price DECIMAL(12, 2) NOT NULL DEFAULT 0, "
                + "product_version_stock INT NOT NULL DEFAULT 0, "
                + "product_version_order INT NOT NULL DEFAULT 10000, "
                + "product_version_image VARCHAR(256) NULL, "
                + "product_version_delete_dt DATETIME NULL, "
                + "UNIQUE KEY ux_product_version_slug (product_id, product_version_slug_addon)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try (Statement stmt = dbService.getConnection().createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    public void add(ProductVersion version) throws SQLException {
        String sql = "INSERT INTO product_versions "
                + "(product_version_id, product_id, product_version_title_addon, product_version_description_addon, "
                + "product_version_slug_addon, product_version_price, product_version_stock, "
                + "product_version_order, product_version_image) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        if (version.getId() == null) {
            version.setId(UUID.randomUUID().toString());
        }
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, version.getId());
            prep.setString(2, version.getProductId());
            prep.setString(3, version.getTitleAddon());
            prep.setString(4, version.getDescriptionAddon());
            prep.setString(5, version.getSlugAddon());
            prep.setBigDecimal(6, version.getPrice());
            prep.setInt(7, version.getStock());
            prep.setInt(8, version.getOrderInPrice());
            prep.setString(9, version.getImageUrl());
            prep.executeUpdate();
        }
    }

    public List<ProductVersion> getByProduct(String productId) throws SQLException {
        String sql = "SELECT * FROM product_versions "
                + "WHERE product_id = ? AND product_version_delete_dt IS NULL "
                + "ORDER BY product_version_order, product_version_title_addon";
        List<ProductVersion> list = new ArrayList<>();
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, productId);
            try (ResultSet rs = prep.executeQuery()) {
                while (rs.next()) {
                    list.add(ProductVersion.fromResultSet(rs));
                }
            }
        }
        return list;
    }

    public boolean isSlugAddonAvailable(String productId, String slugAddon) throws SQLException {
        String sql = "SELECT COUNT(*) FROM product_versions WHERE product_id = ? AND product_version_slug_addon = ?";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, productId);
            prep.setString(2, slugAddon);
            try (ResultSet rs = prep.executeQuery()) {
                return rs.next() && rs.getInt(1) == 0;
            }
        }
    }
}
