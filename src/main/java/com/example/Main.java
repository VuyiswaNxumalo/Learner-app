package com.example;
import com.example.sms.CourseRepository;
import com.example.sms.EnrollmentRepository;
import com.example.sms.SmsApi;
import com.example.sms.StudentRepository;
import  io.javalin.Javalin;

public class Main {

    public static void main(String[] args) {
        LearnerRepository repository = new LearnerRepository();
        repository.createTable();

        Javalin app = Javalin.create(config -> {config.staticFiles.add("/public");});

        app.post("/learners", ctx ->{
            String name = ctx.formParam("name");
            int age = Integer.parseInt(ctx.formParam("age"));
            Learner learner = new Learner(name, age);

            repository.save(learner);
            ctx.redirect("/");

        });

        // Student Management System: make sure its tables exist, then add its API routes
        new StudentRepository().createTable();
        new CourseRepository().createTable();
        new EnrollmentRepository().createTable();
        new SmsApi().register(app);

        app.start(5000);
    }
    
}
