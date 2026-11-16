package com.example.sms;

import io.javalin.Javalin;

public class SmsApi {

    private final CourseRepository courseRepo = new CourseRepository();

    public void register(Javalin app) {
        app.get("/api/courses", ctx -> ctx.json(courseRepo.findAll()));
    }
}