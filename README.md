# University Database Management System (UniDB)

## Project Description

**UniDB** is a Java-based application designed to manage university student records. The system allows users to query a database containing student information such as names, GPAs, majors, and class statistics. The application provides basic and advanced functionality for interacting with the database, including executing custom SQL queries.

## Features

- **Search Students**: Search students by name, year, and GPA (e.g., GPA greater than or equal to a certain value).
- **Department/Class Statistics**: Retrieve statistics for specific departments or classes, such as average GPA or total enrollment.
- **Advanced Queries**: Execute arbitrary SQL queries for custom database interactions, offering flexibility for advanced users.

## Prerequisites

Before running the project, ensure the following prerequisites are met:

- **Java JDK**: Version 1.8 or above.
- **MySQL Database Server**: Install and configure MySQL for student record storage.
- **JDBC Driver for MySQL**: Required to allow the Java application to interact with the MySQL database.

## Database Setup

Before starting the application, ensure the MySQL database is properly set up:

1. **Install MySQL**: If MySQL is not already installed, install it and set up a user account.
2. **Create the Database**:
    - Use the provided SQL script to create and populate the `student_database`. The script will create the necessary tables and insert sample student records.
    - Example command to run the SQL script:
    ```bash
    mysql -u <username> -p < student_database.sql
    ```
3. **Verify Database Setup**:
    - Once the script has been executed, you should see tables like `students`, `departments`, and `classes` in the database.

## Configuration

To connect the UniDB application to your MySQL database, modify the following values in the `UniDB.java` file:

```java
static String DB_URL = "jdbc:mysql://localhost:3306/student_database";
static String USER = "your-username";
static String PASS = "your-password";
```

- **DB_URL**: The URL where your MySQL database is running (typically `localhost:3306` for local setups).
- **USER**: Your MySQL username.
- **PASS**: Your MySQL password.

## Running the Application

Once the prerequisites are installed and the configuration is updated, follow these steps to run the UniDB application:

1. **Compile the Java Project**:
    ```bash
    javac UniDB.java
    ```

2. **Run the Application**:
    ```bash
    java UniDB
    ```

The application will start and allow you to interact with the student database via the terminal.

## Usage

Here are some key operations you can perform within the UniDB application:

- **Search for Students**:
    - Find students by their **name**, **year**, or **GPA** (greater than or equal to, or less than or equal to).
- **Get Department/Class Statistics**:
    - Retrieve statistics like average GPA or student count for a specific department or class.
- **Run Custom SQL Queries**:
    - For advanced database interaction, you can run custom SQL queries directly from the application, allowing for flexibility in data retrieval or modification.

## Example Queries

1. **Search by Name**:
    ```sql
    SELECT * FROM students WHERE name = 'John Doe';
    ```

2. **Find Students with GPA >= 3.5**:
    ```sql
    SELECT * FROM students WHERE GPA >= 3.5;
    ```

3. **Retrieve Statistics for the Computer Science Department**:
    ```sql
    SELECT AVG(GPA) FROM students WHERE major = 'Computer Science';
    ```

## Contributing

Contributions to **UniDB** are welcome! To contribute:

1. **Fork the project**.
2. **Create a feature branch**:
    ```bash
    git checkout -b feature-branch-name
    ```
3. **Commit your changes**:
    ```bash
    git commit -m "Add new feature"
    ```
4. **Push to the branch**:
    ```bash
    git push origin feature-branch-name
    ```
5. **Open a pull request**.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more information.

---

Enjoy using UniDB to manage university student records!
