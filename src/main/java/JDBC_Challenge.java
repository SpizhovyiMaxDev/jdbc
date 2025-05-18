import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.time.LocalDate;
import java.util.InputMismatchException;
import java.util.Locale;
import java.util.UUID;

/*
// ------------- 🔧 Part 1: Create Tables ------------- //

Tables:

books -> book_id, title, author, is_available
members -> member_id, name , email
loans -> loan_id, book_id (forging key), member_id (forging key), loan_date, return_date



// ------------- 🔧 Part 2: Console Menu ------------- //

example of what the console should output:

Choose an option:
1. Add a new book
2. Register a new member
3. Borrow a book
4. Return a book
5. View all books
6. View borrowed books
7. Exit

 */

public class JDBC_Challenge {
    private static String url = "jdbc:postgresql://localhost:13000/jdbc-course";
    private static String username = "postgres";
    private static String password = "Max@132006";
    private static Connection connection;
    private static BufferedReader reader;
    private static boolean programLifecycleIsNotOver = true;

    static {
       try{
           Class.forName("org.postgresql.Driver");
       } catch (ClassNotFoundException e){
           System.out.println("Driver for postgres not found.");
       }
    }

    public static void main(String[] args) {
        try{
            initializeProgramLifeCycle();
        } catch (IOException e){
            printFormatedException("IO exception occurred. More specific reasons: ", e);
        } catch (SQLException e){
            printFormatedException("Database exception occurred. More specific reasons: ", e);
        } catch (Exception e){
            printFormatedException("System error occurred: ", e);
        }
    }

    private static void initializeProgramLifeCycle()  throws IOException, SQLException {
        initializeDatabaseConnection();
        initializeBufferedReader();
        runProgramLifeCycle();
        closeDatabaseConnection();
        closeBufferedReader();
    }

    private static void initializeDatabaseConnection() throws SQLException {
        connection = DriverManager.getConnection(url, username, password);
    }

    private static void initializeBufferedReader() {
        reader = new BufferedReader(new InputStreamReader(System.in));
    }

    private static void runProgramLifeCycle() throws IOException, SQLException {
        while (programLifecycleIsNotOver){
            printOptions();
            int option = getUserOption();
            processUserOption(option);
        }
    }

    private static void printOptions(){
        System.out.println("Choose an option: ");
        System.out.println("1 - Add a new book");
        System.out.println("2 - Delete book");
        System.out.println("3 - Register a new member");
        System.out.println("4 - Borrow a book");
        System.out.println("5 - Return a book");
        System.out.println("6 - View all books");
        System.out.println("7 - View borrowed books");
        System.out.println("8 - Exit");
        System.out.println();
    }

    private static int getUserOption() throws IOException {
        while (true) {
            System.out.print("😃 Enter your choice (1 - 8): ");
            try {
                int option = Integer.parseInt(reader.readLine().trim());

                if (option < 1 || option > 8) {
                    throw new InputMismatchException("Out of valid range.");
                }

                return option;

            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input. Please enter a valid integer.");
            } catch (InputMismatchException e) {
                System.out.println("❌ Please enter a number between 1 and 7." + e.getMessage());
            }
        }
    }

    private static void processUserOption(int option) throws SQLException, IOException {
        switch (option) {
            case 1 -> handleAddNewBook();
            case 2 -> handleDeleteBook();
            case 3 -> handleRegisterNewMember();
            case 4 -> handleBorrowBook();
            case 5 -> handleReturnBook();
            case 6 -> handleViewAllBooks();
            case 7 -> handleViewBorrowedBooks();
            case 8 -> handleExit();
            default -> System.out.println("Invalid option. Please try again.");
        }
    }

    private static void handleAddNewBook() throws SQLException, IOException {
        String title = getBookTitle();
        String author = getBookAuthor();
        boolean isAvailable = getAvailabilityStatus();

        String sqlQuery = "INSERT INTO books (title, author, is_available) VALUES (?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
        preparedStatement.setString(1, title);
        preparedStatement.setString(2, author);
        preparedStatement.setBoolean(3, isAvailable);

        int insertedRows = preparedStatement.executeUpdate();
        displayDatabaseOperationMessage(insertedRows, "Book was added to the database successfully!", "Failed to add the book.");

        preparedStatement.close();
    }

    private static boolean getAvailabilityStatus() throws IOException {
        while (true) {
            System.out.print("Is the book available (yes/no): ");
            String input = reader.readLine().toLowerCase();

            if (input.equals("yes")) {
                return true;
            } else if (input.equals("no")) {
                return false;
            } else {
                System.out.println("Invalid input! Please enter 'yes' or 'no'.");
            }
        }
    }


    private static void handleDeleteBook() throws SQLException, IOException {
        String title = getBookTitle();
        String author = getBookAuthor();

        if(!doesBookExist(title, author)){
            System.out.println("😱Book not found in the database. \n");
            return;
        }

        String sqlQuery = "DELETE FROM books WHERE title = ? AND author = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
        preparedStatement.setString(1, title);
        preparedStatement.setString(2, author);

        int rowWasDeleted = preparedStatement.executeUpdate();
        displayDatabaseOperationMessage(rowWasDeleted, "Books has been successfully deleted.", "Failed to delete the book");
        preparedStatement.close();
    }

    private static boolean doesBookExist(String title, String author) throws SQLException {
        String sqlQuery = "SELECT COUNT(*) FROM books WHERE title = ? AND author = ?";
        PreparedStatement checkStatement = connection.prepareStatement(sqlQuery);
        checkStatement.setString(1, title);
        checkStatement.setString(2, author);

        ResultSet resultSet = checkStatement.executeQuery();
        if (resultSet.next()) return resultSet.getInt(1) > 0;
        resultSet.close();

        return false;
    }

    public static void displayDatabaseOperationMessage(int rowsAffected, String successMessage, String failureMessage) {
        String message = rowsAffected > 0 ? "✨" + successMessage : "🚨" + failureMessage;
        System.out.println(message + "\n");
    }

    private static void handleRegisterNewMember() throws SQLException, IOException {
        String name = getMemberName();
        String email = getMemberEmail();

        String sqlQuery = "INSERT INTO members (name, email) VALUES (?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
        preparedStatement.setString(1, name);
        preparedStatement.setString(2, email);

        int insertedRows = preparedStatement.executeUpdate();
        displayDatabaseOperationMessage(insertedRows, "Member was added to the database successfully!", "Failed to add the member.");

        preparedStatement.close();
    }

    private static void handleBorrowBook() throws SQLException, IOException {
        String email = getMemberEmail();
        int memberId = getMemberId(email);

        if(memberId == -1) {
            System.out.println("\n Sorry but we can't give you a loan for the book, because you are not a member of our college. \n");
            return;
        }

        String title = getBookTitle();
        String author = getBookAuthor();
        int bookId = getBookId(title, author);

        if (bookId == -1) {
            System.out.println("\n 📚 Book not found with this title and author.\n");
            return;
        }

        if(bookIsBorrowed(bookId)){
            System.out.println("\n 😭 Sorry, but this book is currently borrowed by some member. \n");
            return;
        }

        insertLoanIntoTheDatabase(memberId, bookId);
        updateBookAvailability(bookId, false);
    }

    private static String getMemberName() throws IOException {
        System.out.print("Please prompt a name: ");
        return reader.readLine();
    }

    private static  String getMemberEmail() throws IOException {
        System.out.print("Please prompt an email: ");
        return reader.readLine();
    }

    private static String getBookTitle() throws IOException {
        System.out.print("Enter the book title: ");
        return reader.readLine();
    }

    private static String getBookAuthor() throws IOException {
        System.out.print("Enter the book author: ");
        return reader.readLine();
    }

    private static int getBookId(String title, String author) throws SQLException {
        int bookId = -1;

        String sqlQuery = "SELECT book_id FROM books WHERE title = ? AND author = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
        preparedStatement.setString(1, title);
        preparedStatement.setString(2, author);

        ResultSet resultSet = preparedStatement.executeQuery();

        if(resultSet.next()){
            bookId = resultSet.getInt("book_id");
        }

        resultSet.close();
        preparedStatement.close();
        return bookId;
    }

    private static int getMemberId(String email) throws SQLException {
        int memberId = -1;

        String sqlQuery = "SELECT member_id FROM members WHERE email = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
        preparedStatement.setString(1, email);

        ResultSet resultSet = preparedStatement.executeQuery();

        if(resultSet.next()){
            memberId = resultSet.getInt("member_id");
        }

        resultSet.close();
        preparedStatement.close();
        return memberId;
    }

    private static void insertLoanIntoTheDatabase(int memberId, int bookId) throws SQLException {
        LocalDate loanDate = LocalDate.now();
        LocalDate returnDate = loanDate.plusDays(14);
        Date sqlLoanDate = Date.valueOf(loanDate);
        Date sqlReturnDate = Date.valueOf(returnDate);

        String sqlQuery = "INSERT INTO loans (member_id, book_id, loan_date, return_date) VALUES (?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
        preparedStatement.setInt(1, memberId);
        preparedStatement.setInt(2, bookId);
        preparedStatement.setDate(3, sqlLoanDate);
        preparedStatement.setDate(4, sqlReturnDate);

        int insertedRows = preparedStatement.executeUpdate();
        displayDatabaseOperationMessage(insertedRows, "Loan was added to the database successfully!", "Failed to add the loan.");
        preparedStatement.close();
    }

    private static void updateBookAvailability(int bookId, boolean isAvailable) throws SQLException {
        String sqlQuery = "UPDATE books SET is_available = ? WHERE book_id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
        preparedStatement.setBoolean(1, isAvailable);
        preparedStatement.setInt(2, bookId);

        int updatedBooksCount = preparedStatement.executeUpdate();
        displayDatabaseOperationMessage(updatedBooksCount, "Book status was updated in the database successfully!", "Failed to update the book status.");

        preparedStatement.close();
    }

    private static boolean bookIsBorrowed(int bookId) throws SQLException {
        boolean isBorrowed = true;
        String sqlQuery = "SELECT is_available FROM books WHERE book_id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
        preparedStatement.setInt(1, bookId);

        ResultSet resultSet = preparedStatement.executeQuery();

        if(resultSet.next()){
            isBorrowed = !resultSet.getBoolean("is_available");
        }

        resultSet.close();
        preparedStatement.close();

        return isBorrowed;
    }

    private static void handleReturnBook() throws SQLException, IOException {
        String email = getMemberEmail();
        int memberId = getMemberId(email);
        String title = getBookTitle();
        String author = getBookAuthor();
        int bookId = getBookId(title, author);

        if(bookId == -1){
            System.out.println("\n There is no loan for the book with such title or an author \n");
            return;
        }

        handleDeleteLoan(memberId, bookId);
        updateBookAvailability(bookId, true);
    }

    private static void handleDeleteLoan(int memberId, int bookId) throws SQLException {
        String sqlQuery = "DELETE FROM loans WHERE book_id = ? AND member_id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
        preparedStatement.setInt(1, bookId);
        preparedStatement.setInt(2, memberId);

        int deletedLoan = preparedStatement.executeUpdate();
        displayDatabaseOperationMessage(deletedLoan, "Loan is successfully deleted from the database", "Loan doesn't exist in the database.");

        preparedStatement.close();
    }

    private static void handleViewAllBooks() throws SQLException {
        String sqlQuery = "SELECT * FROM books";
        PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);

        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            String title = resultSet.getString("title");
            String author = resultSet.getString("author");
            String availability = resultSet.getBoolean("is_available") ? "available" : "not available";
            System.out.println("Title: " + title + ", author: " + author + ", availability: " + availability);
        }

        System.out.println();
        resultSet.close();
        preparedStatement.close();
    }

    private static void handleViewBorrowedBooks() throws SQLException {
        String sqlQuery = "SELECT * FROM books WHERE is_available = false";
        PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
        ResultSet resultSet = preparedStatement.executeQuery();

        boolean hasResults = false;

        while (resultSet.next()) {
            hasResults = true;
            String title = resultSet.getString("title");
            String author = resultSet.getString("author");
            System.out.printf("❌ Borrowed: %s by %s\n", title, author);
        }

        if (!hasResults) {
            System.out.println("✅ All books are currently available.");
        }

        System.out.println();
        resultSet.close();
        preparedStatement.close();
    }

    public static void handleExit(){
        System.out.println("👋 Exiting the program.");
        programLifecycleIsNotOver = false;
    }

    private static void closeBufferedReader() throws IOException {
        reader.close();
    }

    private static void closeDatabaseConnection() throws SQLException {
        connection.close();
    }

    private static void printFormatedException(String message, Exception e){
        System.out.println(message);
        System.out.println(e.getMessage());
    }
}
