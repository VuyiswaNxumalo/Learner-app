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
                case "7" -> listStudents();
                case "5", "6" -> System.out.println("Coming soon.\n");
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