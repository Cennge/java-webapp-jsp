package step.learning.java231web.data.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import step.learning.java231web.data.dto.ProductSection;
import step.learning.java231web.services.db.IDbService;

/**
 * Розділи товарів (таблиця product_sections). Розділи утворюють дерево
 * через product_section_parent_id; видалення - м'яке (product_section_delete_dt)
 * @author ilyag
 */
@Singleton
public class ProductSectionDao {
    private final IDbService dbService;

    @Inject
    public ProductSectionDao(IDbService dbService) {
        this.dbService = dbService;
    }

    public void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS product_sections ("
                + "product_section_id CHAR(36) PRIMARY KEY, "
                + "product_section_parent_id CHAR(36) NULL, "
                + "product_section_title VARCHAR(128) NOT NULL, "
                + "product_section_description TEXT NULL, "
                + "product_section_slug VARCHAR(128) NOT NULL UNIQUE, "
                + "product_section_image VARCHAR(256) NULL, "
                + "product_section_order INT NOT NULL DEFAULT 10000, "
                + "product_section_delete_dt DATETIME NULL"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try (Statement stmt = dbService.getConnection().createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    public void add(ProductSection section) throws SQLException {
        String sql = "INSERT INTO product_sections "
                + "(product_section_id, product_section_parent_id, product_section_title, "
                + "product_section_description, product_section_slug, product_section_image, product_section_order) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        if (section.getId() == null) {
            section.setId(UUID.randomUUID().toString());
        }
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, section.getId());
            prep.setString(2, section.getParentId());
            prep.setString(3, section.getTitle());
            prep.setString(4, section.getDescription());
            prep.setString(5, section.getSlug());
            prep.setString(6, section.getImageUrl());
            prep.setInt(7, section.getOrderInPrice());
            prep.executeUpdate();
        }
    }

    public int count() throws SQLException {
        try (Statement stmt = dbService.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM product_sections WHERE product_section_delete_dt IS NULL")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Усі розділи плоским списком у порядку відображення */
    public List<ProductSection> getAll() throws SQLException {
        String sql = "SELECT * FROM product_sections WHERE product_section_delete_dt IS NULL "
                + "ORDER BY product_section_order, product_section_title";
        List<ProductSection> list = new ArrayList<>();
        try (Statement stmt = dbService.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(ProductSection.fromResultSet(rs));
            }
        }
        return list;
    }

    /** Кореневі розділи з підрозділами (children) будь-якої глибини */
    public List<ProductSection> getTree() throws SQLException {
        return buildTree(getAll());
    }

    /**
     * Дерево з плоского списку: розділ потрапляє до children свого батька,
     * розділи без батька (або з неіснуючим батьком) - до коренів
     */
    public static List<ProductSection> buildTree(List<ProductSection> flat) {
        Map<String, ProductSection> byId = new LinkedHashMap<>();
        for (ProductSection s : flat) {
            s.setChildren(new ArrayList<>());
            byId.put(s.getId(), s);
        }
        List<ProductSection> roots = new ArrayList<>();
        for (ProductSection s : flat) {
            ProductSection parent = s.getParentId() == null ? null : byId.get(s.getParentId());
            if (parent != null && parent != s) {
                parent.getChildren().add(s);
            } else {
                roots.add(s);
            }
        }
        return roots;
    }

    public ProductSection getBySlug(String slug) throws SQLException {
        String sql = "SELECT * FROM product_sections "
                + "WHERE product_section_slug = ? AND product_section_delete_dt IS NULL";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, slug);
            try (ResultSet rs = prep.executeQuery()) {
                return rs.next() ? ProductSection.fromResultSet(rs) : null;
            }
        }
    }

    /** Безпосередні підрозділи розділу */
    public List<ProductSection> getChildren(String parentId) throws SQLException {
        String sql = "SELECT * FROM product_sections "
                + "WHERE product_section_parent_id = ? AND product_section_delete_dt IS NULL "
                + "ORDER BY product_section_order, product_section_title";
        List<ProductSection> list = new ArrayList<>();
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, parentId);
            try (ResultSet rs = prep.executeQuery()) {
                while (rs.next()) {
                    list.add(ProductSection.fromResultSet(rs));
                }
            }
        }
        return list;
    }

    public boolean existsById(String id) throws SQLException {
        return exists("product_section_id", id);
    }

    public boolean isSlugAvailable(String slug) throws SQLException {
        // UNIQUE-індекс діє і на "видалені" розділи, тому перевіряємо без фільтра видалення
        String sql = "SELECT COUNT(*) FROM product_sections WHERE product_section_slug = ?";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, slug);
            try (ResultSet rs = prep.executeQuery()) {
                return rs.next() && rs.getInt(1) == 0;
            }
        }
    }

    private boolean exists(String column, String value) throws SQLException {
        String sql = "SELECT COUNT(*) FROM product_sections WHERE " + column + " = ? AND product_section_delete_dt IS NULL";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, value);
            try (ResultSet rs = prep.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
