package step.learning.java231web.models;

import java.util.Objects;

/**
 * Елемент переліку доступу користувачів: хто (логін, ім'я, email) та з якою роллю
 * @author ilyag
 */
public class UserAccessItem {
    private String userId;
    private String login;
    private String role;
    private String name;
    private String email;

    public UserAccessItem() {
    }

    public UserAccessItem(String userId, String login, String role, String name, String email) {
        this.userId = userId;
        this.login = login;
        this.role = role;
        this.name = name;
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public UserAccessItem setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getLogin() {
        return login;
    }

    public UserAccessItem setLogin(String login) {
        this.login = login;
        return this;
    }

    public String getRole() {
        return role;
    }

    public UserAccessItem setRole(String role) {
        this.role = role;
        return this;
    }

    public String getName() {
        return name;
    }

    public UserAccessItem setName(String name) {
        this.name = name;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserAccessItem setEmail(String email) {
        this.email = email;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAccessItem that = (UserAccessItem) o;
        return Objects.equals(userId, that.userId)
                && Objects.equals(login, that.login)
                && Objects.equals(role, that.role)
                && Objects.equals(name, that.name)
                && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, login, role, name, email);
    }

    @Override
    public String toString() {
        return "UserAccessItem{userId='" + userId + "', login='" + login + "', role='" + role
                + "', name='" + name + "', email='" + email + "'}";
    }
}
