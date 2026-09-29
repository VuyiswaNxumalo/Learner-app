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
                case "3", "4", "5", "6", "7" -> System.out.println("Coming soon.\n");
                case "0" -> running = false;
                default -> System.out.println("Invalid option, try again.\n");
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
        courseRepo.save(course);
        System.out.println("Course added with ID " + course.getId() + "\n");
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
}