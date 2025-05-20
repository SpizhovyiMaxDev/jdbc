import java.sql.*;

// Download: PostgreSQL JDBC Driver

public class DemoJDBC_1 {
    public static void main(String[] args) {
        try {
            /*
             * 1) Import package
             * 2) Load and register driver
             * 3) Create connection
             * 4) Create statement
             * 5) Execute statement
             * 6) Process the results
             * 7) Close the connection
             * */

            // 1) (Optional) Load Driver class and register driver so that the JDBC DriverManager knows which database driver to use when establishing connection.
            Class.forName("org.postgresql.Driver");

            // 2) Create connection between the database
            String url = "jdbc:postgresql://localhost:13000/jdbc-course";
            // Java uses JDBC to connect to PostgreSQL via a socket or localhost where the database server is running.


            String name = "postgres";
            String password = "-------------";

            Connection connection = DriverManager.getConnection(url, name, password);
            System.out.println("Connection Established");

            // Creates Statement object for sending SQL statements on the database
            Statement statement = connection.createStatement();

//            readNameFromDatabase(statement, 2);
//            insertDataIntoTheStudentsTable(statement);
//            updateStudentName(statement, 6, "Robert (Uncle Bob)");
            deleteStudentById(connection, 7);
            readAllRows(statement);

            statement.close();
            connection.close();
            System.out.println("Connection Closed");
        } catch (ClassNotFoundException e){
            System.out.println("Make sure that class Driver exists, and imported correctly");
        } catch(SQLException e){
            System.out.println("Failed to connect to the database.");
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void readNameFromDatabase(Statement statement, int id) throws SQLException {
        String sqlQuery = "SELECT * " +
                "FROM students " +
                "WHERE student_id = " + id;

        ResultSet result = statement.executeQuery(sqlQuery);

        if(result.next()){ // you are asking the ResultSet to move its internal cursor to the next row in the result set.
            System.out.println("// ------------------------------------------ //");
            String studentName = result.getString("name"); // select column by name or by index
            System.out.println("\t\t Student name is: " + studentName);
        } else {
            System.out.println("// ------------------------------------------ //");
            System.out.println("There was no rows found by hat id.");
        }

        System.out.println("// ------------------------------------------ //");
        System.out.println();
    }

    public static void readAllRows(Statement statement) throws SQLException {
        String sqlQuery = "SELECT * " +
                           "FROM students";

        ResultSet result = statement.executeQuery(sqlQuery);


        System.out.println("// ----------------- All Students ----------------- //");
        while (result.next()){
            int studentId = result.getInt("student_id");
            String studentName = result.getString("name");
            System.out.println("Student id is: " + studentId + ", \nStudent name is: " + studentName + ".\n");
        }

        System.out.println("// ------------------------------------------------ //");
        System.out.println();
    }


    public static void insertDataIntoTheStudentsTable(Statement statement) throws SQLException {
        String sqlQuery = "INSERT INTO students VALUES " +
                           "(5, 'Alice'), " +
                           "(6, 'Bob'), " +
                           "(7, 'Charlie');";

        /*
        * execute()
        * true → the SQL produced a ResultSet (e.g., SELECT)
        * false → the SQL produced no ResultSet (e.g., INSERT, UPDATE, DELETE)
        *
        * executeUpdate()
        * Designed specifically for SQL statements that modify the database, such as INSERT, UPDATE, or DELETE.
        * Returns an int indicating the number of rows affected by the operation. This allows you to verify if the operation was successful.
        * */

        // Just execute the query, if there is a need in the ResultSet, or no need in number of rows affected
//        statement.execute(sqlQuery);

        // Just execute the query, no need in the ResultSet
        int rowsAffected = statement.executeUpdate(sqlQuery);

        if (rowsAffected > 0) {
            System.out.println("Data inserted successfully. " + rowsAffected + " row(s) affected.");
        } else {
            System.out.println("Data insertion failed. No rows were affected.");
        }
    }

    public static void updateStudentName(Statement statement, int studentId, String newName) throws SQLException {
        String sqlQuery = "UPDATE students " +
                    "SET name = '" + newName + "' " +
                    "WHERE student_id = " + studentId;

        int rowsAffected = statement.executeUpdate(sqlQuery);

        if (rowsAffected > 0) {
            System.out.println("✅ Student with ID " + studentId + " was updated to '" + newName + "'.");
        } else {
            System.out.println("⚠️ No student found with ID " + studentId + ".");
        }
    }

    /*
     * Using a PreparedStatement is better because it helps protect your database from harmful attacks called "SQL injection."
     * When you use a PreparedStatement, it safely handles user input and makes sure it can't be used to trick the system into running dangerous code.
     * It also makes your program faster because it can reuse the same query and automatically fixes any special characters in the input.
     * This makes PreparedStatement a safer and more efficient choice compared to using a Statement with manual string building.
     */

    public static void deleteStudentById(Connection connection, int studentId) throws SQLException {
        String sqlQuery = "DELETE FROM students WHERE student_id = ?";

        // prepareStatement() - creates a PreparedStatement object for sending parameterized SQL statements to the database.
        try(PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)){
            preparedStatement.setInt(1, studentId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Student with ID " + studentId + " was deleted successfully.");
            } else {
                System.out.println("No student found with ID " + studentId + ".");
            }
        }
    }

}
