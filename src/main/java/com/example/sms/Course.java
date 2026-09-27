package com.example.sms;

public class Course {
    private int id;
    private String code;   
    private String title;  
    private double fee;

    public Course(String code, String title, double fee) {
        this.code = code;
        this.title = title;
        this.fee = fee;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCode() { return code; }
    public String getTitle() { return title; }
    public double getFee() { return fee; }
}