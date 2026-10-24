package com.example.sms;

import java.util.List;
import java.util.Scanner;

public class ConsoleApp {

    private static final Scanner scanner = new Scanner(System.in);
    private static final StudentRepository studentRepo = new StudentRepository();
    private static final CourseRepository courseRepo = new CourseRepository();
    private static final EnrollmentRepository enrollmentRepo = new EnrollmentRepository();
    private static final EnrollmentService enrollmentService = new EnrollmentService();

    public static void main(String[] args) {
        // Setup: make sure all tables exist before anything else runs
        studentRepo.createTable();
        courseRepo.createTable();
        enrollmentRepo.createTable();

        System.out.println("=== STUDENT MANAGEMENT SYSTEM ===");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> addCourse();
                case "2" -> listCourses();
                case "3" -> registerStudent();
                case "4" -> enrollStudent();
                case "5" -> viewStudentStatus();
                case "6" -> makePayment();
                case "7" -> listStudents();
                case "0" -> running = false;
            }
        }
        System.out.println("Goodbye!");
    }

    private static void printMenu() {
        System.out.println("""
                ------------------------------------
                1. Add new course
                2. List all courses
                3. Register new student
                4. Enroll a student in a course
                5. View a student's status
                6. Make a fee payment
                7. List all students
                0. Exit
                ------------------------------------
                Choose an option:""");
    }

    private static void addCourse() {
        System.out.print("Course code (e.g. CS101): ");
        String code = scanner.nextLine().trim();

        System.out.print("Course title: ");
        String title = scanner.nextLine().trim();

        System.out.print("Course fee: ");
        double fee = readDouble();

        Course course = new Course(code, title, fee);
        Course saved = courseRepo.save(course);

        if (saved == null) {
            System.out.println("A course with code " + code + " already exists.\n");
        } else {
            System.out.println("Course added with ID " + saved.getId() + "\n");
        }
    }

    private static void listCourses() {
        List<Course> courses = courseRepo.findAll();
        if (courses.isEmpty()) {
            System.out.println("No courses yet. Add one with option 1.\n");
            return;
        }
        System.out.println("\nAvailable courses:");
        for (Course c : courses) {
            System.out.printf("  [%d] %s - %s ($%.2f)%n",
                    c.getId(), c.getCode(), c.getTitle(), c.getFee());
        }
        System.out.println();
    }

    private static void listStudents() {
        List<Student> students = studentRepo.findAll();
        if (students.isEmpty()) {
            System.out.println("No students yet. Register one with option 3.\n");
            return;
        }
        System.out.println("\nAll students:");
        for (Student s : students) {
            System.out.printf("  %s | %s | age %d | balance $%.2f%n",
                    s.getStudentId(), s.getName(), s.getAge(), s.getFeeBalance());
        }
        System.out.println();
    }

    private static void registerStudent() {
        System.out.print("Student name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Student age: ");
        int age = readInt();

        System.out.print("Student email: ");
        String email = scanner.nextLine().trim();

        Student student = new Student(name, age, email);
        studentRepo.save(student);

        System.out.println("Student registered! Their ID is " + student.getStudentId() + "\n");
    }

        private static void enrollStudent() {
            System.out.print("Student ID (e.g. STU00001): ");
            String studentId = scanner.nextLine().trim();

            listCourses();   // show the courses so the user can pick an ID

            System.out.print("Course ID to enroll in: ");
            int courseId = readInt();

            try {
                Student student = enrollmentService.enroll(studentId, courseId);
                System.out.printf("Enrolled %s. New fee balance: $%.2f%n%n",
                        student.getName(), student.getFeeBalance());
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage() + "\n");
            }
        }


    private static void viewStudentStatus() {
        System.out.print("Student ID (e.g. STU00001): ");
        String studentId = scanner.nextLine().trim();

        Student student = studentRepo.findByStudentId(studentId);
        if (student == null) {
            System.out.println("No student found with ID " + studentId + "\n");
            return;
        }

        List<Course> courses = enrollmentRepo.findCoursesForStudent(student.getId());

        System.out.println("\n=========================================");
        System.out.println("Student ID  : " + student.getStudentId());
        System.out.println("Name        : " + student.getName());
        System.out.println("Age         : " + student.getAge());
        System.out.println("Email       : " + student.getEmail());
        System.out.printf ("Fee balance : $%.2f%n", student.getFeeBalance());
        System.out.println("Courses     :");
        if (courses.isEmpty()) {
            System.out.println("   (not enrolled in any course yet)");
        } else {
            for (Course c : courses) {
                System.out.println("   - " + c.getCode() + ": " + c.getTitle());
            }
        }
        System.out.println("=========================================\n");
    }

    private static void makePayment() {
    System.out.print("Student ID (e.g. STU00001): ");
    String studentId = scanner.nextLine().trim();

    Student student = studentRepo.findByStudentId(studentId);
    if (student == null) {
        System.out.println("No student found with ID " + studentId + "\n");
        return;
    }

    System.out.printf("%s owes $%.2f%n", student.getName(), student.getFeeBalance());
    if (student.getFeeBalance() == 0) {
        System.out.println("Nothing to pay, the balance is already zero.\n");
        return;
    }

    System.out.print("Payment amount: ");
    double amount = readDouble();

    if (amount <= 0) {
        System.out.println("Payment must be more than zero.\n");
        return;
    }
    if (amount > student.getFeeBalance()) {
        System.out.println("That's more than the balance owed. Payment not recorded.\n");
        return;
    }

    student.makePayment(amount);                                          // step 1: change it in memory
    studentRepo.updateFeeBalance(student.getId(), student.getFeeBalance()); // step 2: save it

    System.out.printf("Payment recorded. New balance: $%.2f%n%n", student.getFeeBalance());
}

    // Keeps asking until the user types a valid number
    private static double readDouble() {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }

    }

    private static int readInt() {
            while (true) {
                try {
                    return Integer.parseInt(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    System.out.print("Please enter a whole number: ");
                }
            }
        }
}