import java.sql.*;

public class DatabaseApp {
    // Databasinställningar
    private static final String URL = "jdbc:postgresql://localhost:5432/testdb";
    private static final String USER = "postgres";
    private static final String PASSWORD = "2579";

    public static void main(String[] args) {
        try {
            // 1. Skapa tabellen 'users' (om den inte redan finns)
            createUserTable();

            // 2. Lägg till några användare
            insertUser("Alice Smith", "alice@example.com", "A");
            insertUser("Bob Johnson", "bob@example.com", "B");

            // 3. Visa alla användare
            listAllUsers();

        } catch (SQLException e) {
            System.err.println("Databasfel: " + e.getMessage());
        }
    }

    private static void createUserTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id SERIAL PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                email VARCHAR(100) UNIQUE NOT NULL,
                grade VARCHAR(10)
            )
        """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("Tabellen 'users' är redo!");
        }
    }

    private static void insertUser(String name, String email, String grade) throws SQLException {
        String sql = "INSERT INTO users (name, email, grade) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, grade);
            pstmt.executeUpdate();
            System.out.println("La till användare: " + name);
        } catch (SQLException e) {
            if (e.getMessage().contains("duplicate key value")) {
                System.out.println("E-postadress finns redan: " + email);
            } else {
                throw e;
            }
        }
    }

    private static void listAllUsers() throws SQLException {
        String sql = "SELECT id, name, email, grade FROM users";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\nAlla användare:");
            System.out.println("----------------------------------------");
            while (rs.next()) {
                System.out.printf("ID: %d | Namn: %s | Email: %s | Betyg: %s\n",
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("grade"));
            }
            System.out.println("----------------------------------------");
        }
    }
}
