package com.example;
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

        app.start(5000);
    }
    
}
