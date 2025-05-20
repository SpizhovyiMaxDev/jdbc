import java.sql.*;


/*
 * Best Practice is to use PreparedStatement, and here is why:
 * 1) Using a PreparedStatement is better because it helps protect your database from harmful attacks called "SQL injection."
 * 2) When you use a PreparedStatement, it safely handles user input and makes sure it can't be used to trick the system into running dangerous code.
 * 3) It also makes your program faster because it can reuse the same query and automatically fixes any special characters in the input.
 * 4) This makes PreparedStatement a safer and more efficient choice compared to using a Statement with manual string building.
 *
 * Copyright by maxweb.studio
 */

public class DemoJDBC_2 {
    private static final String url = "jdbc:postgresql://localhost:13000/jdbc-course";
    private static final String user = "postgres";
    private static final String password = "-------";
    private static Connection connection;

    // For legacy JDBC (usually not needed in modern setups)
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgresSQL driver not found: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            initializeDatabaseConnection();
            performDatabaseOperations();
            closeDatabaseConnection();
        } catch (SQLException e) {
            System.err.println("Database operation failed: \n\n" + e.getMessage());
        }
    }

    public static void initializeDatabaseConnection() throws SQLException {
        connection = DriverManager.getConnection(url, user, password);
    }

    public static void performDatabaseOperations() throws SQLException {
        addNewStudent(6, "Jonas");
    }

    public static void addNewStudent(int id, String name) throws SQLException {
        String sqlQuery = "INSERT INTO students VALUES(?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);

        preparedStatement.setInt(1, id);
        preparedStatement.setString(2, name);

        int rowsCountIncreased = preparedStatement.executeUpdate();

        if(rowsCountIncreased > 0){
            System.out.println("✅ The row has been successfully created in the database.");
        } else {
            System.out.println("⚠️No student has been added.");
        }
    }

    public static void closeDatabaseConnection() throws SQLException {
        connection.close();
    }
}
