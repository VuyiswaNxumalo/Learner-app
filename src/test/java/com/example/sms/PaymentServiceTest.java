package com.example.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PaymentServiceTest {

    private final StudentRepository studentRepo = new StudentRepository();
    private final PaymentService service = new PaymentService();

    private Student john;

    @BeforeEach
    void useFreshDatabase(@TempDir Path tempDir) {
        System.setProperty("sms.db.url", "jdbc:sqlite:" + tempDir.resolve("test.db"));
        studentRepo.createTable();

        john = studentRepo.save(new Student("John", 20, "john@example.com"));
        studentRepo.updateFeeBalance(john.getId(), 200.00);   // John owes $200
    }

    @AfterEach
    void restoreRealDatabase() {
        System.clearProperty("sms.db.url");
    }

    private double storedBalance() {
        return studentRepo.findByStudentId(john.getStudentId()).getFeeBalance();
    }

    @Test
    void partialPaymentReducesTheStoredBalance() {
        service.pay(john.getStudentId(), 50.00);

        assertEquals(150.00, storedBalance(), 0.001);
    }

    @Test
    void payingTheFullBalanceLeavesZero() {
        service.pay(john.getStudentId(), 200.00);

        assertEquals(0.0, storedBalance(), 0.001);
    }

    @Test
    void overpaymentIsRejectedAndBalanceIsUnchanged() {
        assertThrows(IllegalArgumentException.class,
                () -> service.pay(john.getStudentId(), 500.00));

        assertEquals(200.00, storedBalance(), 0.001);
    }

    @Test
    void zeroAndNegativePaymentsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.pay(john.getStudentId(), 0));
        assertThrows(IllegalArgumentException.class, () -> service.pay(john.getStudentId(), -25.00));

        assertEquals(200.00, storedBalance(), 0.001);
    }

    @Test
    void payingWhenNothingIsOwedIsRejected() {
        service.pay(john.getStudentId(), 200.00);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.pay(john.getStudentId(), 10.00));

        assertTrue(e.getMessage().contains("no balance"));
    }

    @Test
    void unknownStudentIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.pay("STU99999", 10.00));

        assertTrue(e.getMessage().contains("STU99999"));
    }
}