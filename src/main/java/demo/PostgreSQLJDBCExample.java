package demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PostgreSQLJDBCExample {
    public static void main(String[] args) {
        // 1. Database connection parameters
        final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";
        final String DB_USER = "postgres";
        final String DB_PASSWORD = "2579";
        
        // 2. Use try-with-resources for automatic resource management
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement()) {
            
            // Load JDBC driver (not strictly needed in newer JDBC versions)
            Class.forName("org.postgresql.Driver");
            
            System.out.println("Successfully connected to the database!");
            
            // 3. Execute SQL query
            String sqlQuery = "SELECT id, name FROM users";
            try (ResultSet resultSet = statement.executeQuery(sqlQuery)) {
                
                // 4. Process the results
                System.out.println("\nUsers in the database:");
                while (resultSet.next()) {
                    int userId = resultSet.getInt("id");
                    String userName = resultSet.getString("name");
                    System.out.printf("ID: %d, Name: %s%n", userId, userName);
                }
            }
            
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database connection error:");
            e.printStackTrace();
        }
    }
}

