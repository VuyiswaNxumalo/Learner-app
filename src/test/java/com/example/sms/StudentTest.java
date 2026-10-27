package com.example.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class StudentTest {

    @Test
    void newStudentStartsWithZeroBalanceAndNoIds() {
        Student s = new Student("John", 20, "john@example.com");

        assertEquals(0.0, s.getFeeBalance(), 0.001);
        assertEquals(0, s.getId());
        assertNull(s.getStudentId());
    }

    @Test
    void addFeeIncreasesBalance() {
        Student s = new Student("John", 20, "john@example.com");

        s.addFee(150.00);
        s.addFee(90.00);

        assertEquals(240.00, s.getFeeBalance(), 0.001);
    }

    @Test
    void makePaymentReducesBalance() {
        Student s = new Student("John", 20, "john@example.com");
        s.addFee(200.00);

        s.makePayment(50.00);

        assertEquals(150.00, s.getFeeBalance(), 0.001);
    }

    @Test
    void makePaymentNeverGoesBelowZero() {
        Student s = new Student("John", 20, "john@example.com");
        s.addFee(100.00);

        s.makePayment(500.00);

        assertEquals(0.0, s.getFeeBalance(), 0.001);
    }
}