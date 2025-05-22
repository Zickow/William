package demo;

// UserDatabase.java
import java.sql.*;
import java.io.IOException;
import java.nio.file.*;
import java.time.*;

public class UserDatabase {
    private static final String URL = "jdbc:postgresql://localhost:5432/min_databas";
    private static final String USER = "postgres";
    private static final String PASSWORD = "ditt_lösenord"; // Byt till ditt riktiga lösenord
    private static final String LOG_FILE = "user_errors.log";

    public static void main(String[] args) {
        createTable();
        insertSampleData();   // Lärarens testdata
        testOperations();     // Manuella testmetoder
    }

    private static void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id SERIAL PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                email VARCHAR(100) UNIQUE NOT NULL,
                grade CHAR(2) CHECK (grade IN ('AA', 'A', 'B', 'C', 'D', 'E')),
                position VARCHAR(50) NOT NULL,
                hire_date DATE DEFAULT CURRENT_DATE
            )""";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("✅ Tabell skapad!");
        } catch (SQLException e) {
            logError("Tabellfel", e);
        }
    }

    private static void insertSampleData() {
        String[][] users = {
            {"Noah", "AA", "Entry Level"},
            {"Luna", "C", "Entry Level"},
            {"Felix", "B", "Entry Level"},
            {"Milo", "D", "Senior"},
            {"Jace", "AA", "Entry Level"},
            {"Aria", "C", "Entry Level"},
            {"Nico", "C", "Entry Level"},
            {"Rhett", "B", "Manager"},
            {"Ivy", "E", "Intern"},
            {"Ezra", "A", "Senior"},
            {"Skye", "A", "Entry Level"},
            {"Rowan", "B", "Entry Level"},
            {"Kai", "C", "Entry Level"},
            {"Levi", "C", "Entry Level"},
            {"Zion", "C", "Entry Level"},
            {"Tate", "C", "Entry Level"},
            {"Finn", "B", "Entry Level"},
            {"Cruz", "B", "Entry Level"},
            {"Zane", "C", "Entry Level"},
            {"Reid", "B", "Entry Level"},
            {"Eden", "C", "Entry Level"},
            {"Blake", "B", "Manager"},
            {"Nova", "C", "Entry Level"},
            {"Otis", "B", "Entry Level"},
            {"Kian", "D", "Entry Level"},
            {"Dante", "D", "Entry Level"},
            {"River", "AA", "Senior Manager"},
            {"Beau", "A", "Director"},
            {"Reign", "A", "Director"},
            {"Sage", "A", "Director"}
        };

        for (String[] user : users) {
            try {
                String sql = """
                    INSERT INTO users (name, email, grade, position)
                    VALUES (?, ?, ?, ?)""";

                try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    pstmt.setString(1, user[0]);
                    pstmt.setString(2, user[0].toLowerCase() + "@example.com");
                    pstmt.setString(3, user[1]);
                    pstmt.setString(4, user[2]);

                    pstmt.executeUpdate();
                    System.out.println("✅ La till " + user[0]);
                }
            } catch (SQLException e) {
                logError("Kunde inte lägga till " + user[0], e);
            }
        }
    }

    private static void addUser(String name, String email, String grade) {
        String sql = "INSERT INTO users (name, email, grade, position) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, grade);
            pstmt.setString(4, "Okänd");

            int rows = pstmt.executeUpdate();
            System.out.println(rows > 0 ? "👤 Användare tillagd!" : "❌ Misslyckades");
        } catch (SQLException e) {
            handleError(e, email);
        }
    }

    private static void updateGrade(String email, String newGrade) {
        String sql = "UPDATE users SET grade = ? WHERE email = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newGrade);
            pstmt.setString(2, email);

            int rows = pstmt.executeUpdate();
            System.out.println(rows > 0 ? "✏️ Betyg uppdaterat!" : "❌ Ingen användare hittades");
        } catch (SQLException e) {
            handleError(e, email);
        }
    }

    private static void deleteUser(String email) {
        String sql = "DELETE FROM users WHERE email = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);

            int rows = pstmt.executeUpdate();
            System.out.println(rows > 0 ? "🗑️ Användare raderad!" : "❌ Ingen användare hittades");
        } catch (SQLException e) {
            handleError(e, email);
        }
    }

    private static void showUsers() {
        String sql = "SELECT id, name, email, grade, position, hire_date FROM users ORDER BY id";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n📋 Användarlista:");
            while (rs.next()) {
                System.out.printf(
                    "🆔 %d | 👤 %s | 📧 %s | ⭐ %s | 🧑‍💼 %s | 📅 %s\n",
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("grade"),
                    rs.getString("position"),
                    rs.getDate("hire_date")
                );
            }
        } catch (SQLException e) {
            logError("Visningsfel", e);
        }
    }

    private static void showStats() {
        String sql = "SELECT grade, COUNT(*) as count FROM users GROUP BY grade ORDER BY grade";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n📊 Statistik:");
            while (rs.next()) {
                System.out.printf("⭐ %s → %d användare\n", rs.getString("grade"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            logError("Statistikfel", e);
        }
    }

    private static void handleError(SQLException e, String email) {
        String errorMsg = e.getMessage();

        if (errorMsg.contains("duplicate key")) {
            System.out.println("🚫 E-post finns redan: " + email);
        } else if (errorMsg.contains("check constraint")) {
            System.out.println("❌ Ogiltigt betyg! Använd A, B, C, D, E eller AA");
        } else if (errorMsg.contains("null value")) {
            System.out.println("⚠️ Namn/e-post får inte vara tomt");
        } else {
            System.out.println("💥 Databasfel: " + errorMsg);
        }
        logError(errorMsg, e);
    }

    private static void logError(String message, Exception e) {
        try {
            String logEntry = String.format(
                "[%s] %s: %s\n",
                LocalDateTime.now(),
                message,
                e.getMessage()
            );
            Files.writeString(
                Path.of(LOG_FILE),
                logEntry,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            );
        } catch (IOException ioException) {
            System.err.println("📛 Loggningsfel: " + ioException.getMessage());
        }
    }

    private static void testOperations() {
        addUser("Anna Andersson", "anna@example.com", "A");
        addUser("Bo Bengtsson", "bo@example.com", "B");
        addUser("Bo Bengtsson", "bo@example.com", "B");
        addUser("Kalle", "kalle@test.com", "X");
        addUser("", "tom@test.com", "C");
        addUser("Pelle", null, "A");

        updateGrade("anna@example.com", "B");

        deleteUser("bo@example.com");

        showUsers();
        showStats();
    }
}
