package step.learning.java231web.models;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class UserSignupFormModel {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^[A-Za-zА-Яа-яІіЇїЄєҐґ'\\-\\s]{2,50}$"
    );
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*\\p{L})(?=.*\\d).+$"
    );
    private static final Pattern LOGIN_PATTERN = Pattern.compile(
            "^[A-Za-z0-9_.-]{3,30}$"
    );

    private String name;
    private String email;
    private String login;
    private String password;
    private String repeatPassword;
    private boolean agree;

    public UserSignupFormModel() {
    }

    public UserSignupFormModel(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public UserSignupFormModel(String name, String email, String login) {
        this.name = name;
        this.email = email;
        this.login = login;
    }

    public UserSignupFormModel(String name, String email, String password, String repeatPassword, boolean agree) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.repeatPassword = repeatPassword;
        this.agree = agree;
    }

    public UserSignupFormModel(String name, String email, String login, String password, String repeatPassword, boolean agree) {
        this.name = name;
        this.email = email;
        this.login = login;
        this.password = password;
        this.repeatPassword = repeatPassword;
        this.agree = agree;
    }

    public String getName() {
        return name;
    }

    public UserSignupFormModel setName(String name) {
        this.name = name;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserSignupFormModel setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getLogin() {
        return login;
    }

    public UserSignupFormModel setLogin(String login) {
        this.login = login;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public UserSignupFormModel setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getRepeatPassword() {
        return repeatPassword;
    }

    public UserSignupFormModel setRepeatPassword(String repeatPassword) {
        this.repeatPassword = repeatPassword;
        return this;
    }

    public boolean isAgree() {
        return agree;
    }

    public UserSignupFormModel setAgree(boolean agree) {
        this.agree = agree;
        return this;
    }

    public Map<String, String> getValidationErrors() {
        Map<String, String> errors = new LinkedHashMap<>();

        if (name == null || name.trim().isEmpty()) {
            errors.put("name", "Ім'я не може бути порожнім");
        } else if (name.trim().length() < 2) {
            errors.put("name", "Ім'я повинно містити щонайменше 2 символи");
        } else if (!NAME_PATTERN.matcher(name.trim()).matches()) {
            errors.put("name", "Ім'я містить неприпустимі символи");
        }

        if (email == null || email.trim().isEmpty()) {
            errors.put("email", "Email не може бути порожнім");
        } else if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            errors.put("email", "Некоректний формат email");
        }

        if (login != null && !login.trim().isEmpty()) {
            if (login.trim().length() < 3) {
                errors.put("login", "Логін повинен містити щонайменше 3 символи");
            } else if (!LOGIN_PATTERN.matcher(login.trim()).matches()) {
                errors.put("login", "Логін містить неприпустимі символи");
            }
        }

        if (password != null) {
            if (password.length() < 6) {
                errors.put("password", "Пароль повинен містити щонайменше 6 символів");
            } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
                errors.put("password", "Пароль повинен містити літери та цифри");
            }
            if (repeatPassword != null && !password.equals(repeatPassword)) {
                errors.put("repeatPassword", "Паролі не збігаються");
            }
        }

        // для повної форми (з паролем) обов'язкова згода з правилами
        if (password != null && !agree) {
            errors.put("agree", "Необхідно погодитись з правилами сайту");
        }

        return Collections.unmodifiableMap(errors);
    }

    public boolean isValid() {
        return getValidationErrors().isEmpty();
    }

    public void validate() throws IllegalArgumentException {
        Map<String, String> errors = getValidationErrors();
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errors.values()));
        }
    }
}
