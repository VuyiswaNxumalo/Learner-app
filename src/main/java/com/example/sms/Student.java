package com.example.sms;

public class Student {
    private int id;              
    private String studentId;    
    private String name;
    private int age;
    private String email;
    private double feeBalance;

    public Student(String name, int age, String email) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.feeBalance = 0.0;
    }

    // Used when rebuilding a saved student from the database
    public Student(int id, String studentId, String name, int age, String email, double feeBalance) {
        this.id = id;
        this.studentId = studentId;
        this.name = name;
        this.age = age;
        this.email = email;
        this.feeBalance = feeBalance;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getName() { return name; }
    public int getAge() { return age; }
    public String getEmail() { return email; }

    public double getFeeBalance() { return feeBalance; }

    public void addFee(double amount) {
        this.feeBalance += amount;
    }

    public void makePayment(double amount) {
        this.feeBalance = Math.max(0, this.feeBalance - amount);
    }
}