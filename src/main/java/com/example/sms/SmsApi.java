package com.example.sms;

import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.Map;

public class SmsApi {

    private final CourseRepository courseRepo = new CourseRepository();
    private final StudentRepository studentRepo = new StudentRepository();

    // The shapes of the JSON a client sends. Boxed types (Double, Integer) so a missing value is null, not 0.
    record NewCourse(String code, String title, Double fee) {}
    record NewStudent(String name, Integer age, String email) {}

    public void register(Javalin app) {
        // Turn exceptions into status codes. The most specific handler wins.
        app.exception(NotFoundException.class, (e, ctx) -> error(ctx, 404, e.getMessage()));
        app.exception(ConflictException.class, (e, ctx) -> error(ctx, 409, e.getMessage()));
        app.exception(IllegalArgumentException.class, (e, ctx) -> error(ctx, 400, e.getMessage()));

        app.get("/api/courses", ctx -> ctx.json(courseRepo.findAll()));
        app.post("/api/courses", this::addCourse);

        app.get("/api/students", this::listStudents);
        app.post("/api/students", this::registerStudent);
    }

    private void addCourse(Context ctx) {
        NewCourse body = ctx.bodyAsClass(NewCourse.class);

        if (body.code() == null || body.code().isBlank()) {
            throw new IllegalArgumentException("code is required");
        }
        if (body.title() == null || body.title().isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        if (body.fee() == null) {
            throw new IllegalArgumentException("fee is required");
        }
        if (body.fee() < 0) {
            throw new IllegalArgumentException("fee can't be negative");
        }

        String code = body.code().trim();
        Course saved = courseRepo.save(new Course(code, body.title().trim(), body.fee()));
        if (saved == null) {                       // save() returns null for a duplicate code
            throw new ConflictException("A course with code " + code + " already exists");
        }
        ctx.status(201).json(saved);
    }

    private void listStudents(Context ctx) {
        String search = ctx.queryParam("search");   // the part after ?search= in the address
        if (search == null || search.isBlank()) {
            ctx.json(studentRepo.findAll());
        } else {
            ctx.json(studentRepo.search(search));
        }
    }

    private void registerStudent(Context ctx) {
        NewStudent body = ctx.bodyAsClass(NewStudent.class);

        if (body.name() == null || body.name().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (body.age() == null) {
            throw new IllegalArgumentException("age is required");
        }
        if (body.age() < 1 || body.age() > 120) {
            throw new IllegalArgumentException("age must be between 1 and 120");
        }
        String email = body.email() == null ? "" : body.email().trim();
        if (!email.isEmpty() && !email.contains("@")) {
            throw new IllegalArgumentException("email doesn't look valid");
        }

        Student saved = studentRepo.save(new Student(body.name().trim(), body.age(), email));
        ctx.status(201).json(saved);                // includes the generated studentId
    }

    private static void error(Context ctx, int status, String message) {
        ctx.status(status).json(Map.of("error", message));
    }
}