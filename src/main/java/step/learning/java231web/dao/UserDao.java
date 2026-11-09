package step.learning.java231web.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import step.learning.java231web.models.AuthUser;
import step.learning.java231web.models.UserAccessItem;
import step.learning.java231web.models.UserSignupFormModel;
import step.learning.java231web.rest.RestPagination;
import step.learning.java231web.services.db.IDbService;
import step.learning.java231web.services.kdf.IKdfService;
import step.learning.java231web.services.kdf.Pbkdf2KdfService;

@Singleton
public class UserDao {
    private final IDbService dbService;
    private final IKdfService kdfService;
    private final Logger logger;

    @Inject
    public UserDao(IDbService dbService, IKdfService kdfService) {
        this.dbService = dbService;
        this.kdfService = kdfService;
        this.logger = Logger.getLogger(UserDao.class.getName());
    }

    public UserDao(IDbService dbService) {
        this(dbService, new Pbkdf2KdfService());
    }

    public UserDao(IDbService dbService, Logger logger) {
        this.dbService = dbService;
        this.kdfService = new Pbkdf2KdfService();
        this.logger = logger;
    }

    public void install() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS user ("
                + "id CHAR(36) PRIMARY KEY, "
                + "name VARCHAR(128) NOT NULL, "
                + "email VARCHAR(128) NOT NULL, "
                + "login VARCHAR(64) NULL, "
                + "salt CHAR(32) NULL, "     // сіль для KDF
                + "dk CHAR(64) NULL, "       // похідний ключ (derived key) пароля
                + "role VARCHAR(32) NOT NULL DEFAULT 'user', "   // роль доступу: admin, moderator, user, guest
                + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + "deleted_at DATETIME NULL"
                + ")";
        try (Statement statement = dbService.getConnection().createStatement()) {
            statement.executeUpdate(sql);
            // міграція таблиць, створених до появи паролів та ролей
            addColumnIfMissing(statement, "salt", "CHAR(32) NULL");
            addColumnIfMissing(statement, "dk", "CHAR(64) NULL");
            addColumnIfMissing(statement, "role", "VARCHAR(32) NOT NULL DEFAULT 'user'");
        }
    }

    private void addColumnIfMissing(Statement statement, String column, String definition) throws SQLException {
        try (ResultSet rs = statement.executeQuery(
                "SELECT COUNT(*) FROM information_schema.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = '" + column + "'")) {
            rs.next();
            if (rs.getInt(1) > 0) {
                return;
            }
        }
        statement.executeUpdate("ALTER TABLE user ADD COLUMN " + column + " " + definition);
    }

    public int getUsersCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM user WHERE deleted_at IS NULL";
        try (Statement statement = dbService.getConnection().createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Користувач за ідентифікатором (для перевірки ?ua=&lt;id&gt; на домашній сторінці)
     * @return AuthUser або null, якщо такого (не видаленого) користувача немає
     */
    public AuthUser getAuthUserById(String id) throws SQLException {
        String sql = "SELECT id, COALESCE(login, name) AS login, name, email, role FROM user "
                + "WHERE id = ? AND deleted_at IS NULL";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, id);
            try (ResultSet rs = prep.executeQuery()) {
                return rs.next()
                        ? new AuthUser(rs.getString("id"), rs.getString("login"), rs.getString("name"),
                                rs.getString("email"), rs.getString("role"))
                        : null;
            }
        }
    }

    /**
     * Перелік доступу користувачів (логін, роль) - сторінка pagination,
     * нові користувачі першими
     */
    public List<UserAccessItem> getUsersAccess(RestPagination pagination) throws SQLException {
        String sql = "SELECT id, COALESCE(login, name) AS login, role, name, email FROM user "
                + "WHERE deleted_at IS NULL "
                + "ORDER BY created_at DESC, id "
                + "LIMIT ? OFFSET ?";
        List<UserAccessItem> list = new ArrayList<>();
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setInt(1, pagination.getPageSize());
            prep.setInt(2, pagination.getOffset());
            try (ResultSet rs = prep.executeQuery()) {
                while (rs.next()) {
                    list.add(new UserAccessItem(
                            rs.getString("id"),
                            rs.getString("login"),
                            rs.getString("role"),
                            rs.getString("name"),
                            rs.getString("email")));
                }
            }
        }
        return list;
    }

    /**
     * Автентифікація за логіном (або email) та паролем
     * @return дані користувача (id, name, email, login) або null,
     *         якщо користувача немає чи пароль не збігається
     */
    public Map<String, Object> authenticate(String login, String password) throws SQLException {
        if (isBlank(login) || password == null) {
            return null;
        }
        String sql = "SELECT id, name, email, login, role, salt, dk FROM user "
                + "WHERE (login = ? OR email = ?) AND deleted_at IS NULL";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, login.trim());
            prep.setString(2, login.trim());
            try (ResultSet rs = prep.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                String salt = rs.getString("salt");
                String dk = rs.getString("dk");
                if (salt == null || dk == null) {
                    return null;   // користувач без пароля (створений до його появи)
                }
                // порівняння за сталий час - захист від timing-атак
                boolean match = java.security.MessageDigest.isEqual(
                        dk.getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                        kdfService.dk(password, salt).getBytes(java.nio.charset.StandardCharsets.US_ASCII));
                if (!match) {
                    return null;
                }
                Map<String, Object> user = new LinkedHashMap<>();
                user.put("id", rs.getString("id"));
                user.put("name", rs.getString("name"));
                user.put("email", rs.getString("email"));
                user.put("login", rs.getString("login"));
                user.put("role", rs.getString("role"));
                return user;
            }
        }
    }

    public boolean isLoginAvailable(String login) throws SQLException {
        if (login == null || login.trim().isEmpty()) {
            return false;
        }
        String trimmed = login.trim();
        String sql = "SELECT COUNT(*) FROM user WHERE (login = ? OR email = ?) AND (deleted_at IS NULL)";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, trimmed);
            prep.setString(2, trimmed);
            try (ResultSet rs = prep.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        }
        return false;
    }

    public boolean isEmailAvailable(String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String trimmed = email.trim();
        String sql = "SELECT COUNT(*) FROM user WHERE email = ? AND (deleted_at IS NULL)";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, trimmed);
            try (ResultSet rs = prep.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        }
        return false;
    }

    /**
     * Перевірка унікальності (доступності) логіна та email
     * як частина валідації форми реєстрації.
     * Якщо логін не вказано, логіном стає ім'я - тоді помилка
     * прив'язується до поля "name"
     * @return помилки у форматі "поле - повідомлення" (порожня, якщо все вільне)
     */
    public Map<String, String> getUniquenessErrors(UserSignupFormModel formModel) throws SQLException {
        Map<String, String> errors = new LinkedHashMap<>();
        boolean hasLogin = !isBlank(formModel.getLogin());
        String checkLogin = hasLogin ? formModel.getLogin().trim() : formModel.getName();
        if (!isBlank(checkLogin) && !isLoginAvailable(checkLogin)) {
            errors.put(hasLogin ? "login" : "name", "Логін '" + checkLogin.trim() + "' вже зайнятий");
        }
        if (!isBlank(formModel.getEmail()) && !isEmailAvailable(formModel.getEmail())) {
            errors.put("email", "Email '" + formModel.getEmail().trim() + "' вже зареєстровано");
        }
        return errors;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public void signupUser(UserSignupFormModel formModel) throws SQLException {
        if (formModel == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }
        formModel.validate();

        String checkLogin = formModel.getLogin() != null && !formModel.getLogin().trim().isEmpty()
                ? formModel.getLogin().trim()
                : formModel.getName().trim();

        if (!isLoginAvailable(checkLogin)) {
            throw new IllegalArgumentException("Логін '" + checkLogin + "' вже зайнятий");
        }

        if (!isEmailAvailable(formModel.getEmail())) {
            throw new IllegalArgumentException("Email '" + formModel.getEmail() + "' вже зайнятий");
        }

        String sql = "INSERT INTO user(id, name, email, login, salt, dk) VALUES (?, ?, ?, ?, ?, ?)";
        // у БД - лише сіль та похідний ключ, сам пароль не зберігається
        String salt = null, dk = null;
        if (formModel.getPassword() != null) {
            salt = kdfService.newSalt();
            dk = kdfService.dk(formModel.getPassword(), salt);
        }
        String userId = UUID.randomUUID().toString();
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, userId);
            prep.setString(2, formModel.getName());
            prep.setString(3, formModel.getEmail());
            prep.setString(4, checkLogin);
            prep.setString(5, salt);
            prep.setString(6, dk);
            prep.executeUpdate();
        }
        catch (SQLException ex) {
            logger.log(Level.WARNING, sql, ex);
            throw ex;
        }
    }
}
