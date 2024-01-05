# University Database Management System (UniDB)

## Project Description
UniDB is a Java-based application for managing university student records. It allows users to query a database containing student information such as names, GPAs, majors, and class statistics. 

## Features
- Search students by name, year, GPA (greater than or equal to, less than or equal to).
- Get statistics for a specific department or class.
- Execute arbitrary SQL queries for advanced database interactions.

## Prerequisites
- Java JDK 1.8 or above.
- MySQL Database Server.
- JDBC Driver for MySQL.

## Database Setup
Before running the application, ensure that the MySQL database is set up and running. Use the provided SQL scripts to create and populate the `student_database`.

## Configuration
The database URL, username, and password are configured in the `UniDB` class. Modify these values to match your database setup:
  ```bash
      static String DB_URL = "";
      static String USER = "";
      static String PASS = "";



