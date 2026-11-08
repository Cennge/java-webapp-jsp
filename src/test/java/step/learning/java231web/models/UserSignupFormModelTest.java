package step.learning.java231web.models;

import org.junit.jupiter.api.Test;
import step.learning.java231web.dao.UserDao;
import step.learning.java231web.services.db.IDbService;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class UserSignupFormModelTest {

    @Test
    public void testValidModelPassesValidation() {
        UserSignupFormModel model = new UserSignupFormModel(
                "Иван Иванов",
                "ivan.ivanov@example.com",
                "ivan_231",
                "Password123",
                "Password123",
                true
        );

        assertTrue(model.isValid());
        assertTrue(model.getValidationErrors().isEmpty());
        assertDoesNotThrow(model::validate);
    }

    @Test
    public void testNullNameThrowsException() {
        UserSignupFormModel model = new UserSignupFormModel(null, "ivan@example.com");

        assertFalse(model.isValid());
        Map<String, String> errors = model.getValidationErrors();
        assertTrue(errors.containsKey("name"));
        assertThrows(IllegalArgumentException.class, model::validate);
    }

    @Test
    public void testEmptyAndBlankNameFails() {
        UserSignupFormModel model1 = new UserSignupFormModel("", "ivan@example.com");
        UserSignupFormModel model2 = new UserSignupFormModel("   ", "ivan@example.com");

        assertFalse(model1.isValid());
        assertFalse(model2.isValid());
        assertTrue(model1.getValidationErrors().containsKey("name"));
        assertTrue(model2.getValidationErrors().containsKey("name"));
    }

    @Test
    public void testShortNameFails() {
        UserSignupFormModel model = new UserSignupFormModel("A", "ivan@example.com");

        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("name"));
        assertThrows(IllegalArgumentException.class, model::validate);
    }

    @Test
    public void testNullEmailFails() {
        UserSignupFormModel model = new UserSignupFormModel("Олександр", null);

        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("email"));
        assertThrows(IllegalArgumentException.class, model::validate);
    }

    @Test
    public void testInvalidEmailFormatFails() {
        String[] invalidEmails = {"notanemail", "user@", "@domain.com", "user@domain", "user@.com"};

        for (String invalidEmail : invalidEmails) {
            UserSignupFormModel model = new UserSignupFormModel("Олександр", invalidEmail);
            assertFalse(model.isValid(), "Email should be invalid: " + invalidEmail);
            assertTrue(model.getValidationErrors().containsKey("email"));
            assertThrows(IllegalArgumentException.class, model::validate);
        }
    }

    @Test
    public void testValidLoginFormatPasses() {
        UserSignupFormModel model = new UserSignupFormModel(
                "Олександр",
                "oleksandr@example.com",
                "admin_231"
        );

        assertTrue(model.isValid());
        assertNull(model.getValidationErrors().get("login"));
    }

    @Test
    public void testShortLoginFormatFails() {
        UserSignupFormModel model = new UserSignupFormModel(
                "Олександр",
                "oleksandr@example.com",
                "ab"
        );

        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("login"));
        assertThrows(IllegalArgumentException.class, model::validate);
    }

    @Test
    public void testInvalidCharactersInLoginFails() {
        UserSignupFormModel model = new UserSignupFormModel(
                "Олександр",
                "oleksandr@example.com",
                "user 123!"
        );

        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("login"));
        assertThrows(IllegalArgumentException.class, model::validate);
    }

    @Test
    public void testPasswordMismatchFails() {
        UserSignupFormModel model = new UserSignupFormModel(
                "Олександр",
                "oleksandr@example.com",
                "secret123",
                "secret321",
                true
        );

        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("repeatPassword"));
        assertThrows(IllegalArgumentException.class, model::validate);
    }

    @Test
    public void testShortPasswordFails() {
        UserSignupFormModel model = new UserSignupFormModel(
                "Олександр",
                "oleksandr@example.com",
                "123",
                "123",
                true
        );

        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("password"));
        assertThrows(IllegalArgumentException.class, model::validate);
    }

    @Test
    public void testPasswordWithoutDigitsOrLettersFails() {
        UserSignupFormModel onlyLetters = new UserSignupFormModel(
                "Олександр", "oleksandr@example.com", "password", "password", true);
        UserSignupFormModel onlyDigits = new UserSignupFormModel(
                "Олександр", "oleksandr@example.com", "12345678", "12345678", true);

        assertTrue(onlyLetters.getValidationErrors().containsKey("password"));
        assertTrue(onlyDigits.getValidationErrors().containsKey("password"));
    }

    @Test
    public void testAgreeRequiredForFullForm() {
        UserSignupFormModel model = new UserSignupFormModel(
                "Олександр", "oleksandr@example.com", "secret123", "secret123", false);

        assertFalse(model.isValid());
        assertEquals(1, model.getValidationErrors().size());
        assertTrue(model.getValidationErrors().containsKey("agree"));
    }

    @Test
    public void testEmptyFormCollectsAllRequiredFieldErrors() {
        UserSignupFormModel model = new UserSignupFormModel("", "", "", "", "", false);

        Map<String, String> errors = model.getValidationErrors();
        assertTrue(errors.containsKey("name"));
        assertTrue(errors.containsKey("email"));
        assertTrue(errors.containsKey("password"));
        assertTrue(errors.containsKey("agree"));
        assertFalse(errors.containsKey("login"), "Логін необов'язковий");
    }

    @Test
    public void testUserDaoRejectsNullAndInvalidModelBeforeDatabaseCall() {
        UserDao userDao = new UserDao(null);

        assertThrows(IllegalArgumentException.class, () -> userDao.signupUser(null));

        UserSignupFormModel invalidModel = new UserSignupFormModel("", "invalid-email");
        assertThrows(IllegalArgumentException.class, () -> userDao.signupUser(invalidModel));
    }

    @Test
    public void testLoginUniquenessValidationInUserDao() throws SQLException {
        IDbService mockDbService = createMockDbService("admin");
        UserDao userDao = new UserDao(mockDbService);

        assertFalse(userDao.isLoginAvailable("admin"), "Existing login 'admin' should not be available");
        assertTrue(userDao.isLoginAvailable("new_user_231"), "New login should be available");

        UserSignupFormModel duplicateModel = new UserSignupFormModel(
                "Тестовий Користувач",
                "test@example.com",
                "admin"
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userDao.signupUser(duplicateModel)
        );
        assertTrue(ex.getMessage().contains("вже зайнятий"));

        UserSignupFormModel availableModel = new UserSignupFormModel(
                "Тестовий Користувач",
                "unique@example.com",
                "new_user_231"
        );

        assertDoesNotThrow(() -> userDao.signupUser(availableModel));
    }

    @Test
    public void testUniquenessErrorsAreReportedPerField() throws SQLException {
        UserDao userDao = new UserDao(createMockDbService("admin"));

        Map<String, String> taken = userDao.getUniquenessErrors(
                new UserSignupFormModel("Тестовий", "test@example.com", "admin"));
        assertTrue(taken.containsKey("login"));
        assertTrue(taken.get("login").contains("вже зайнятий"));

        Map<String, String> free = userDao.getUniquenessErrors(
                new UserSignupFormModel("Тестовий", "test@example.com", "new_user_231"));
        assertTrue(free.isEmpty());

        // без логіна логіном стає ім'я - помилка прив'язується до поля "name"
        Map<String, String> byName = userDao.getUniquenessErrors(
                new UserSignupFormModel("admin", "test@example.com", ""));
        assertTrue(byName.containsKey("name"));

        // порожні поля до БД не потрапляють (і не дають NPE)
        assertDoesNotThrow(() -> userDao.getUniquenessErrors(new UserSignupFormModel(null, null)));
    }

    private IDbService createMockDbService(final String takenLogin) {
        return () -> (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if ("prepareStatement".equals(method.getName())) {
                        final String[] boundParams = new String[2];
                        return Proxy.newProxyInstance(
                                PreparedStatement.class.getClassLoader(),
                                new Class<?>[]{PreparedStatement.class},
                                (pStmt, stmtMethod, stmtArgs) -> {
                                    if ("setString".equals(stmtMethod.getName())) {
                                        int idx = (Integer) stmtArgs[0];
                                        if (idx <= 2) {
                                            boundParams[idx - 1] = (String) stmtArgs[1];
                                        }
                                        return null;
                                    }
                                    if ("executeQuery".equals(stmtMethod.getName())) {
                                        boolean isTaken = takenLogin != null && (
                                                takenLogin.equalsIgnoreCase(boundParams[0]) ||
                                                takenLogin.equalsIgnoreCase(boundParams[1])
                                        );
                                        final int count = isTaken ? 1 : 0;
                                        return Proxy.newProxyInstance(
                                            ResultSet.class.getClassLoader(),
                                            new Class<?>[]{ResultSet.class},
                                            (rs, rsMethod, rsArgs) -> {
                                                if ("next".equals(rsMethod.getName())) {
                                                    return true;
                                                }
                                                if ("getInt".equals(rsMethod.getName())) {
                                                    return count;
                                                }
                                                if ("close".equals(rsMethod.getName())) {
                                                    return null;
                                                }
                                                return null;
                                            }
                                        );
                                    }
                                    if ("executeUpdate".equals(stmtMethod.getName())) {
                                        return 1;
                                    }
                                    if ("close".equals(stmtMethod.getName())) {
                                        return null;
                                    }
                                    return null;
                                }
                        );
                    }
                    if ("close".equals(method.getName())) {
                        return null;
                    }
                    return null;
                }
        );
    }
}
