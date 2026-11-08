package step.learning.java231web.services.db;

import com.google.inject.Singleton;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Singleton
public class MySqlDbService implements IDbService {
    private final String connectionString = "jdbc:mysql://localhost:3306/java_231?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8&allowPublicKeyRetrieval=true";
    private final String user = "user_231j";
    private final String password = "pass_231";
    private Connection connection;

    public MySqlDbService() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(connectionString, user, password);
        }
        return connection;
    }
}
