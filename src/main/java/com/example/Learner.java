package com.example;

public class Learner {
    private int id;
    private String name ;
    private int age;

    public Learner( String name , int age){
   
        this.age = age;
        this.name = name;
    }

      public Learner(int id , String name , int age){
        this.id = id;
        this.age = age;
        this.name = name;
    }

    public String getName(){
        return name;
    }
    
    public  int getId(){
        return id;
    }
    
    public int getAge(){
        return age;
    }

    public void setId(int id){
        this.id = id;
    }
}
