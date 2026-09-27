package com.example.sms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Db {
    public static final String URL = "jdbc:sqlite:learner.db";

    private Db() {}

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}