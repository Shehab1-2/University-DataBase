import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class UniDB {
    static String DB_URL = ""; // insert data base url
    static String USER = ""; // Insert data base user
    static String PASS = ""; // Insert data base password
    static Scanner scanner = new Scanner(System.in);
    static Scanner userInput = new Scanner(System.in);
    static int input;
    static Connection conn = null;

    public static void main(String[] args) {
        if (args.length > 0) {
            DB_URL = args[0];
            USER = args[1];
            PASS = args[2];
        } else {
            System.out.println("No args passed");
        }
        // Connection conn = null;
        Statement stmt = null;
        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection("jdbc:mysql://" + DB_URL, USER, PASS);
            System.out.println("Connected..");

            // MENU AND RUNNER METHODS
            menu();
            int x = userInput.nextInt();
            runner(x);

            // Clean-up environment
            // rs.close();
            // stmt.close();
            conn.close();
        } catch (SQLException se) {
            // Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            // Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            // finally block used to close resources
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException se2) {
            } // nothing we can do
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        System.out.println("Goodbye!");
    }

    public static void runner(int x) {
        switch (x) {
            case 1:
                searchStudentsByName(conn, scanner);
                break;
            case 2:
                searchStudentsByYear(conn, scanner);
                break;
            case 3:
                searchStudentsByGpaGreaterThanOrEqual(conn, scanner);
                break;
            case 4:
                searchStudentsByGpaLessThanOrEqual(conn, scanner);
                break;
            case 5:
                getDepartmentStatistics(conn, scanner);
                break;
            case 6:
                getClassStatistics(conn, scanner);
                break;
            case 7:
                executeArbitrarySqlQuery(conn, scanner);
                break;
            case 8:
                System.out.println("Exiting the application.");
                break;
            default:
                System.out.println("Invalid option. Please choose a number between 1 and 8.");
        }
    }

    // Prints a menu of options to the user
    public static void menu() {
        System.out.println("Welcome to the university database. Queries available:");
        System.out.println("1. Search students by name.");
        System.out.println("2. Search students by year.");
        System.out.println("3. Search for students with a GPA >= threshold.");
        System.out.println("4. Search for students with a GPA <= threshold.");
        System.out.println("5. Get department statistics.");
        System.out.println("6. Get class statistics.");
        System.out.println("7. Execute an arbitrary SQL query.");
        System.out.println("8. Exit the application.");
        System.out.print("Which query would you like to run (1-8)? ");
    }

    // Prints student information from a ResultSet
    public static void printStudentInfo(ResultSet rs) {
        try {
            while (rs.next()) {
                int id = rs.getInt("id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String major = rs.getString("major");
                String minor = rs.getString("minor");
                double gpa = rs.getDouble("gpa");
                int totalCreditsTaking = rs.getInt("total_credits_taking");

                // Print the student information
                System.out.println(lastName + ", " + firstName);
                System.out.println("ID: " + id);
                System.out.println("Major(s): " + major);
                System.out.println("Minor(s): " + minor);
                System.out.printf("GPA: %.2f%n", gpa);
                System.out.println("Total Credits Taking: " + totalCreditsTaking);
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // (a) Search students by name
    public static void searchStudentsByName(Connection conn, Scanner scanner) {
        System.out.print("Enter the name to search: ");
        String searchString = scanner.next();

        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT s.id, s.first_name, s.last_name, " +
                        "majors.minors AS major, " +
                        "minors.minors AS minor, " +
                        "SUM(case when ht.grade = 'A' then 4 when ht.grade = 'B' then 3 when ht.grade = 'C' then 2 when ht.grade = 'D' then 1 else 0 end * c_credits.credits) / SUM(c_credits.credits) AS gpa, "
                        +
                        "IFNULL(SUM(taking_credits.credits), 0) AS total_credits_taking " +
                        "FROM Students s " +
                        "LEFT JOIN (SELECT sid, GROUP_CONCAT(DISTINCT dname ORDER BY dname SEPARATOR '/') AS minors FROM Majors GROUP BY sid) AS majors ON s.id = majors.sid "
                        +
                        "LEFT JOIN (SELECT sid, GROUP_CONCAT(DISTINCT dname ORDER BY dname SEPARATOR '/') AS minors FROM Minors GROUP BY sid) AS minors ON s.id = minors.sid "
                        +
                        "LEFT JOIN HasTaken ht ON s.id = ht.sid " +
                        "LEFT JOIN Classes c_credits ON ht.name = c_credits.name " +
                        "LEFT JOIN (SELECT sid, SUM(c.credits) AS credits FROM IsTaking JOIN Classes c ON IsTaking.name = c.name GROUP BY sid) AS taking_credits ON s.id = taking_credits.sid "
                        +
                        "WHERE s.first_name LIKE ? OR s.last_name LIKE ? " +
                        "GROUP BY s.id, s.first_name, s.last_name, majors.minors, minors.minors")) {
            stmt.setString(1, "%" + searchString + "%");
            stmt.setString(2, "%" + searchString + "%");
            ResultSet rs = stmt.executeQuery();

            // Call the printStudentInfo method to print the search results
            printStudentInfo(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // (b) Search students by year
    public static void searchStudentsByYear(Connection conn, Scanner scanner) {
        System.out.print("Enter the year (Fr, So, Ju, Sr): ");
        String year = scanner.next();

        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT s.id, s.first_name, s.last_name, " +
                        "       CASE " +
                        "           WHEN total_credits >= 0 AND total_credits <= 29 THEN 'Fr' " +
                        "           WHEN total_credits >= 30 AND total_credits <= 59 THEN 'So' " +
                        "           WHEN total_credits >= 60 AND total_credits <= 89 THEN 'Ju' " +
                        "           ELSE 'Sr' " +
                        "       END AS student_year, " +
                        "       total_credits " +
                        "FROM Students s " +
                        "JOIN ( " +
                        "    SELECT ht.sid, SUM(c.credits) AS total_credits " +
                        "    FROM HasTaken ht " +
                        "    JOIN Classes c ON ht.name = c.name " +
                        "    WHERE ht.grade <> 'F' " +
                        "    GROUP BY ht.sid " +
                        ") AS credit_totals ON s.id = credit_totals.sid " +
                        "HAVING student_year = ?")) {
            stmt.setString(1, year);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String studentYear = rs.getString("student_year");
                int totalCredits = rs.getInt("total_credits");

                // Print the student information
                System.out.println(lastName + ", " + firstName);
                System.out.println("ID: " + id);
                System.out.println("Student Year: " + studentYear);
                System.out.println("Total Credits: " + totalCredits);
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // (c) Search for students with a GPA equal to or above a given threshold
    public static void searchStudentsByGpaGreaterThanOrEqual(Connection conn, Scanner scanner) {
        System.out.println("Enter the minimum GPA threshold:");
        double threshold = scanner.nextDouble();
        scanner.nextLine(); // Consume the newline character after the double input

        String query = "WITH GradePoints AS ("
                + "SELECT"
                + "    sid,"
                + "    SUM(credits * CASE grade"
                + "      WHEN 'A' THEN 4"
                + "      WHEN 'B' THEN 3"
                + "      WHEN 'C' THEN 2"
                + "      WHEN 'D' THEN 1"
                + "      ELSE 0"
                + "    END) AS total_points,"
                + "    SUM(credits) AS total_credits"
                + "  FROM"
                + "    HasTaken"
                + "    JOIN Classes ON HasTaken.name = Classes.name"
                + "  GROUP BY"
                + "    sid"
                + "),"
                + "StudentGPAs AS ("
                + "  SELECT"
                + "    id,"
                + "    first_name,"
                + "    last_name,"
                + "    COALESCE(GROUP_CONCAT(Majors.dname), '-') AS major,"
                + "    COALESCE(GROUP_CONCAT(Minors.dname), '-') AS minor,"
                + "    (total_points / total_credits) AS gpa,"
                + "    COALESCE(SUM(Classes.credits), 0) AS total_credits_taking"
                + "  FROM"
                + "    Students"
                + "    LEFT JOIN Majors ON Students.id = Majors.sid"
                + "    LEFT JOIN Minors ON Students.id = Minors.sid"
                + "    LEFT JOIN IsTaking ON Students.id = IsTaking.sid"
                + "    JOIN Classes ON IsTaking.name = Classes.name"
                + "    JOIN GradePoints ON Students.id = GradePoints.sid"
                + "  GROUP BY"
                + "    id"
                + ")"
                + "SELECT *"
                + "FROM StudentGPAs"
                + " WHERE gpa >= ?";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setDouble(1, threshold);
            ResultSet rs = stmt.executeQuery();

            printStudentInfo(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // (d) Search for students with a GPA equal to or below a given threshold
    public static void searchStudentsByGpaLessThanOrEqual(Connection conn, Scanner scanner) {
        System.out.println("Enter the maximum GPA threshold:");
        double threshold = scanner.nextDouble();
        scanner.nextLine(); // Consume the newline character after the double input

        String query = "WITH GradePoints AS ("
                + "SELECT"
                + "    sid,"
                + "    SUM(credits * CASE grade"
                + "      WHEN 'A' THEN 4"
                + "      WHEN 'B' THEN 3"
                + "      WHEN 'C' THEN 2"
                + "      WHEN 'D' THEN 1"
                + "      ELSE 0"
                + "    END) AS total_points,"
                + "    SUM(credits) AS total_credits"
                + "  FROM"
                + "    HasTaken"
                + "    JOIN Classes ON HasTaken.name = Classes.name"
                + "  GROUP BY"
                + "    sid"
                + "),"
                + "StudentGPAs AS ("
                + "  SELECT"
                + "    id,"
                + "    first_name,"
                + "    last_name,"
                + "    COALESCE(GROUP_CONCAT(Majors.dname), '-') AS major,"
                + "    COALESCE(GROUP_CONCAT(Minors.dname), '-') AS minor,"
                + "    (total_points / total_credits) AS gpa,"
                + "    COALESCE(SUM(Classes.credits), 0) AS total_credits_taking"
                + "  FROM"
                + "    Students"
                + "    LEFT JOIN Majors ON Students.id = Majors.sid"
                + "    LEFT JOIN Minors ON Students.id = Minors.sid"
                + "    LEFT JOIN IsTaking ON Students.id = IsTaking.sid"
                + "    JOIN Classes ON IsTaking.name = Classes.name"
                + "    JOIN GradePoints ON Students.id = GradePoints.sid"
                + "  GROUP BY"
                + "    id"
                + ")"
                + "SELECT *"
                + "FROM StudentGPAs"
                + " WHERE gpa <= ?";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setDouble(1, threshold);
            ResultSet rs = stmt.executeQuery();

            printStudentInfo(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // (e) For a given department, report its number of students and the average of
    // those students' GPAs
    public static void getDepartmentStatistics(Connection conn, Scanner scanner) {
        System.out.print("Enter the department: ");
        String department = scanner.nextLine();
        String query = "SELECT COUNT(*) as num_students, AVG(gpa) as avg_gpa " +
                "FROM (SELECT sid, SUM(credits * grade_point) / SUM(credits) AS gpa " +
                "      FROM (SELECT sid, hasTaken.name, grade, credits, " +
                "                   CASE grade WHEN 'A' THEN 4 WHEN 'B' THEN 3 " +
                "                                WHEN 'C' THEN 2 WHEN 'D' THEN 1 ELSE 0 END AS grade_point " +
                "            FROM hasTaken JOIN classes ON hasTaken.name = classes.name) AS temp " +
                "      GROUP BY sid) AS gpa_table " +
                "WHERE sid IN (SELECT sid FROM majors WHERE dname = ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            // "SELECT COUNT(*) as num_students, AVG(gpa) as avg_gpa FROM (SELECT sid,
            // SUM(credits * grade_point) / SUM(credits) AS gpa FROM (SELECT sid, name,
            // grade, credits, CASE grade WHEN 'A' THEN 4 WHEN 'B' THEN 3 WHEN 'C' THEN 2
            // WHEN 'D' THEN 1 ELSE 0 END AS grade_point FROM hasTaken JOIN classes ON
            // hasTaken.name = classes.name) AS temp GROUP BY sid) AS gpa_table WHERE sid IN
            // (SELECT sid FROM majors WHERE dname = ?)")) {
            stmt.setString(1, department);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int numStudents = rs.getInt("num_students");
                double avgGpa = rs.getDouble("avg_gpa");

                System.out.println("Department: " + department);
                System.out.println("Number of students: " + numStudents);
                System.out.printf("Average GPA: %.2f%n", avgGpa);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // (f) For a given class, report the number of students currently/Letter grade
    public static void getClassStatistics(Connection conn, Scanner scanner) {
        System.out.print("Enter the class name: ");
        String className = scanner.nextLine();

        try {
            // Get the number of students currently taking the class
            try (PreparedStatement stmt = conn
                    .prepareStatement("SELECT COUNT(*) as num_students FROM IsTaking WHERE name = ?")) {
                stmt.setString(1, className);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int numStudents = rs.getInt("num_students");
                    System.out.println("Number of students currently taking the class: " + numStudents);
                }
            }

            // Get the number of students who've gotten each letter grade
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT grade, COUNT(*) as num_students FROM HasTaken WHERE name = ? GROUP BY grade ORDER BY grade")) {
                stmt.setString(1, className);
                ResultSet rs = stmt.executeQuery();

                System.out.println("Grade distribution for students who have taken the class:");
                while (rs.next()) {
                    String grade = rs.getString("grade");
                    int numStudents = rs.getInt("num_students");
                    System.out.println("Grade " + grade + ": " + numStudents);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // (g) Execute an arbitrary SQL query
    public static void executeArbitrarySqlQuery(Connection conn, Scanner scanner) {
        System.out.print("Enter your SQL query: ");
        String query = scanner.nextLine();

        try (Statement stmt = conn.createStatement()) {
            boolean hasResultSet = stmt.execute(query);

            if (hasResultSet) {
                ResultSet rs = stmt.getResultSet();
                ResultSetMetaData metadata = rs.getMetaData();
                int columnCount = metadata.getColumnCount();

                // Print column names
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(metadata.getColumnName(i) + "\t");
                }
                System.out.println();

                // Print result set
                while (rs.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        System.out.print(rs.getString(i) + "\t");
                    }
                    System.out.println();
                }
            } else {
                int updateCount = stmt.getUpdateCount();
                System.out.println("Update count: " + updateCount);
            }
        } catch (SQLException e) {
            System.out.println("Error executing the query: " + e.getMessage());
        }
    }

}