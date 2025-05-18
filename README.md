# 📚 Java JDBC Playground

This repository contains a series of Java console applications demonstrating how to interact with a PostgreSQL database using **JDBC (Java Database Connectivity)**. The goal of this project was to practice database connection handling, CRUD operations, SQL safety with `PreparedStatement`, and console-based interaction logic.

---

## 🔧 Technologies Used

- **Java 17+**
- **PostgreSQL 14+**
- **JDBC Driver** (`org.postgresql.Driver`)

---

## 📁 Project Structure

### 1. `DemoJDBC_1.java` – JDBC Fundamentals

Demonstrates:
- Connecting to a PostgreSQL database using `DriverManager`
- Executing SQL queries using `Statement`
- Safely executing parameterized SQL using `PreparedStatement`
- CRUD operations: `SELECT`, `INSERT`, `UPDATE`, `DELETE`

💡 Includes a full step-by-step JDBC connection process:
```java
1. Load the driver class
2. Create a connection
3. Create a statement
4. Execute SQL commands
5. Process results
6. Close the connection
