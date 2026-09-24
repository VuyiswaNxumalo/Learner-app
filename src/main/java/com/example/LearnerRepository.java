package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class LearnerRepository {
    private static final String DATABASE_URL = "jdbc:sqlite:learner.db";

    public void createTable(){
        String sql = """
                CREATE TABLE IF NOT EXISTS learners(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL ,
                age INTEGER NOT NULL)
                """;
        
        try(Connection connection = DriverManager.getConnection(DATABASE_URL);
            Statement statement = connection.createStatement()){
                statement.execute(sql);

            } catch (Exception e){
                throw new RuntimeException(e);
            }
    }
    
    public void save(Learner learner){
        String sql = """
                INSERT INTO learners(name , age)
                VALUES(? , ?)
                """;
        try(Connection connection = DriverManager.getConnection(DATABASE_URL);
            PreparedStatement statement = connection.prepareStatement(sql)){
                statement.setString(1, learner.getName());
                statement.setInt(2, learner.getAge());
                statement.executeUpdate();


            } catch (Exception e){
                throw new RuntimeException(e);
            }
    }
}
