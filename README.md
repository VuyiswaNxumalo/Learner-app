# Learner App + Student Management System

A Java project with two parts that share one SQLite database:

1. **Learner App** – a small web app (Javalin) where users submit a learner's name and age through an HTML form.
2. **Student Management System** – a console app that registers students, enrolls them in courses, generates unique student IDs, tracks fee balances, and shows each student's status.

The Student Management System was built step by step as a learning project, with the design decisions made deliberately (see [Design decisions](#design-decisions)).

## Tech stack

- Java 17
- Maven
- Javalin 6 (web framework, Learner App only)
- SQLite via `sqlite-jdbc`
- JUnit 5 (tests)

## How to run

Run all commands from the project root (the folder containing `pom.xml`). Always use the same folder, because the database file `learner.db` is created in the directory you run from.

**Learner App (web, runs on http://localhost:5000):**

```bash
mvn clean compile exec:java
```

**Student Management System (console):**

```bash
mvn compile exec:java@sms-console
```

**Tests:**

```bash
mvn test
```

## Student Management System

### Menu

```
1. Add new course
2. List all courses
3. Register new student
4. Enroll a student in a course
5. View a student's status
6. Make a fee payment
7. List all students
0. Exit
```

### How it works

- **Courses** are added through the menu (code, title, fee). Course codes are unique.
- **Registering a student** generates a unique ID such as `STU00001`.
- **Enrolling** a student adds the course fee to their balance. A student can't be enrolled in the same course twice, and the fee is only charged on a successful first enrollment.
- **Payments** reduce the balance. A payment must be above zero and can't exceed the amount owed.
- **Status** shows personal info, student ID, fee balance, and enrolled courses.

## Project structure

```
src/main/java/com/example/
├── Main.java                    Learner App entry point (Javalin)
├── Learner.java                 Learner model
├── LearnerRepository.java       Learner database access
└── sms/
    ├── ConsoleApp.java          Console entry point: menu, input, output
    ├── Student.java             Student model (fee balance rules live here)
    ├── Course.java              Course model
    ├── Db.java                  Shared SQLite connection helper
    ├── StudentRepository.java   Students table, ID generation
    ├── CourseRepository.java    Courses table
    ├── EnrollmentRepository.java  Student-course links (join table)
    └── EnrollmentService.java   Enrollment rules (checks, then fee)

src/test/java/com/example/sms/   JUnit tests
src/main/resources/public/       HTML and CSS for the Learner App
```

### Database tables

| Table | Purpose |
|---|---|
| `learners` | Learner App submissions |
| `students` | Student info, public `student_id`, fee balance |
| `courses` | Course code, title, fee |
| `enrollments` | Links students to courses; `UNIQUE(student_id, course_id)` |

All tables live in `learner.db`.

## Design decisions

- **Two IDs per student.** `id` is the internal number SQLite generates; `studentId` (`STU00001`) is the public ID built from it.
- **Students don't hold a list of courses.** The `enrollments` table is the single source of truth, so there is no copy that can go stale. Courses are loaded with `findCoursesForStudent()`.
- **No `Enrollment` class.** Nothing in the app uses an enrollment as an object, so `EnrollmentRepository` writes the link rows directly.
- **No `setFeeBalance()`.** The balance only changes through `addFee()` and `makePayment()`, so it can't be set to an invalid value.
- **Duplicates are caught by the database.** The `UNIQUE` constraint decides what counts as a duplicate, and the code turns that into a friendly message instead of a crash.
- **Enroll first, charge second.** `EnrollmentService` charges the fee only after the enrollment succeeds, so a duplicate never costs the student money.
- **Rules live in a service class.** The console menu only asks questions and prints results, so the same logic can be reused by a web version later.

## Testing

Tests use a temporary SQLite file per test, so your real `learner.db` is never touched. `Db` reads the `sms.db.url` system property and falls back to `learner.db` when it isn't set.

## Known limitations

- Enrollment and the fee update are two separate writes, not one transaction. A crash between them could leave a student enrolled but not charged.
- Fees are stored as `double`, which can introduce rounding errors. `BigDecimal` or whole cents would be safer for real money.
- Duplicate detection checks for `"UNIQUE"` in the database error message, which depends on the driver's wording.
- Student input is only lightly validated (for example, blank names are accepted).
- There is no way to edit or delete students or courses.

## Possible next steps

- Web routes for the Student Management System, reusing the same repositories and `EnrollmentService`
- A transaction around enrollment and the fee update
- Input validation for names, emails, and ages
- More tests, including `EnrollmentService`
