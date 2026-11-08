package step.learning.java231web.services.db;

import java.sql.Connection;
import java.sql.SQLException;

public interface IDbService {
    Connection getConnection() throws SQLException;
}
