import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class PopulateDatabase {
    static String DB_URL = ""; // insert data base url
    static String USER = ""; // Insert data base user
    static String PASS = ""; // Insert data base password

    static Scanner scanner = new Scanner(System.in);
    static Scanner userInput = new Scanner(System.in);
    static int input;
    static Connection conn = null;
    static Map<String, Integer> classCredits = fetchClassCredits();
    static String[] className = classCredits.keySet().toArray(new String[0]);
    static String[] classNames = {
            "Introduction to Biology",
            "Genetics and Evolution",
            "Molecular Biology",
            "Ecology and Conservation",
            "Microbiology",
            "Plant Biology",
            "Human Anatomy and Physiology",
            "Animal Behavior",
            // Chem:
            "General Chemistry",
            "Organic Chemistry",
            "Inorganic Chemistry",
            "Physical Chemistry",
            "Analytical Chemistry",
            "Biochemistry",
            "Environmental Chemistry",
            "Polymer Chemistry",
            // CS:
            "Introduction to Computer Science",
            "Data Structures and Algorithms",
            "Computer Networks",
            "Operating Systems",
            "Software Engineering",
            "Artificial Intelligence",
            "Web Development",
            "Database Systems",
            // Eng:
            "Introduction to Engineering",
            "Mechanics of Materials",
            "Thermodynamics",
            "Fluid Mechanics",
            "Electrical Circuits",
            "Materials Science",
            "Control Systems",
            "Engineering Design",
            // Math:
            "Calculus I",
            "Calculus II",
            "Linear Algebra",
            "Differential Equations",
            "Discrete Mathematics",
            "Probability and Statistics",
            "Numerical Analysis",
            "Abstract Algebra",
            // Phys:
            "General Physics I",
            "General Physics II",
            "Classical Mechanics",
            "Electromagnetism",
            "Thermodynamics and Statistical Mechanics",
            "Quantum Mechanics",
            "Optics",
            "Nuclear Physics", };

    static String[] firstNames = { "John", "Jane", "Michael", "Sarah", "David", "Emily", "Robert", "Jessica", "Daniel",
            "Elizabeth" };
    static String[] lastNames = { "Smith", "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis", "Garcia",
            "Rodriguez", "Wilson" };
    static String[] departmentNames = { "Bio", "Chem", "CS", "Eng", "Math", "Phys" };

    public static void main(String[] args) {
        // Connection conn = null;
        Statement stmt = null;
        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Connected..");

            Set<Integer> uniqueId = populate();
            // Call hasTaken and isTaking for each student
            for (Integer studentId : uniqueId) {
                hasTaken(studentId);
                isTaking(studentId);
            }

            // Clean-up environment
            // rs.close();
            // stmt.close();
            // 1 conn.close();
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
    }

    public static void hasTaken(int studentId) {
        int numClassesTaken;
        Random ran = new Random();
        Set<String> uniqueClasses = new HashSet<>();
        int totalCredits = 0;
        // Determine the number of classes taken based on the student's year
        if (totalCredits <= 29) { // Freshmen
            numClassesTaken = ran.nextInt(8) + 3;
        } else if (totalCredits <= 59) { // Sophomores
            numClassesTaken = ran.nextInt(6) + 10;
        } else if (totalCredits <= 89) { // Juniors
            numClassesTaken = ran.nextInt(6) + 15;
        } else { // Seniors
            numClassesTaken = ran.nextInt(6) + 20;
        }

        // Populate hasTaken table
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement stmt = conn
                        .prepareStatement("INSERT INTO hasTaken (sid, name, grade) VALUES (?, ?, ?)")) {

            while (uniqueClasses.size() < numClassesTaken && totalCredits <= 120) {
                String candidateClass = classNames[ran.nextInt(classNames.length)];
                int candidateCredits = classCredits.get(candidateClass);

                if (!uniqueClasses.contains(candidateClass) && totalCredits + candidateCredits <= 120) {
                    uniqueClasses.add(candidateClass);
                    totalCredits += candidateCredits;
                }
            }

            // Insert unique classes into hasTaken table
            for (String takenClass : uniqueClasses) {
                String grade = "ABCDF".charAt(ran.nextInt(5)) + "";
                stmt.setInt(1, studentId);
                stmt.setString(2, takenClass);
                stmt.setString(3, grade);
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void isTaking(int studentId) {
        Random ran = new Random();

        // Populate isTaking table
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO isTaking (sid, name) VALUES (?, ?)")) {

            int numClassesTaking = 3; // Every student is taking 3 classes
            Set<String> uniqueClasses = new HashSet<>();

            while (uniqueClasses.size() < numClassesTaking) {
                String candidateClass = className[ran.nextInt(className.length)];

                if (!uniqueClasses.contains(candidateClass)) {
                    uniqueClasses.add(candidateClass);
                }
            }

            // Insert unique classes into isTaking table
            for (String takingClass : uniqueClasses) {
                stmt.setInt(1, studentId);
                stmt.setString(2, takingClass);
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Integer> fetchClassCredits() {
        Map<String, Integer> classCredits = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT name, credits FROM Classes")) {

            while (rs.next()) {
                classCredits.put(rs.getString("name"), rs.getInt("credits"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return classCredits;
    }

    public static Set<Integer> populate() {

        int numStud = 100;
        Random ran = new Random();
        Set<Integer> uniqueId = new HashSet<>();
        // generates unique ids
        while (uniqueId.size() < numStud) {
            int studentId = 100000000 + ran.nextInt(900000000);
            uniqueId.add(studentId);

            String firstName = firstNames[ran.nextInt(firstNames.length)];
            String lastName = lastNames[ran.nextInt(lastNames.length)];

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                // insert student record into the database
                try (PreparedStatement stmt = conn
                        .prepareStatement("INSERT INTO students (id, first_name, last_name) VALUES (?, ?, ?)")) {
                    stmt.setInt(1, studentId);
                    stmt.setString(2, firstName);
                    stmt.setString(3, lastName);
                    stmt.executeUpdate();
                }

                // assign one or 2 majors
                try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO majors (sid, dname) VALUES (?, ?)")) {
                    int numMajors = ran.nextInt(2) + 1;
                    for (int i = 0; i < numMajors; i++) {
                        String major = departmentNames[ran.nextInt(departmentNames.length)];
                        stmt.setInt(1, studentId);
                        stmt.setString(2, major);
                        stmt.executeUpdate();
                    }
                }

                // assign zero or more minors
                try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO minors (sid, dname) VALUES (?, ?)")) {
                    int numMinors = ran.nextInt(3);
                    for (int i = 0; i < numMinors; i++) {
                        String minor = departmentNames[ran.nextInt(departmentNames.length)];
                        stmt.setInt(1, studentId);
                        stmt.setString(2, minor);
                        stmt.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return uniqueId;
    }
}