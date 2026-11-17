package com.example.sms;

public class PaymentService {

    private final StudentRepository studentRepo = new StudentRepository();

    /**
     * Records a payment against a student's fee balance.
     * Throws IllegalArgumentException with a readable message if the payment isn't allowed.
     */
    public Student pay(String studentId, double amount) {
        Student student = studentRepo.findByStudentId(studentId);
        if (student == null) {
            throw new NotFoundException("No student found with ID " + studentId);
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment must be more than zero.");
        }
        if (student.getFeeBalance() == 0) {
            throw new ConflictException(student.getName() + " has no balance to pay.");
        }
        if (amount > student.getFeeBalance()) {
            throw new ConflictException(
                    String.format("That's more than the balance owed ($%.2f). Payment not recorded.",
                            student.getFeeBalance()));
        }

        student.makePayment(amount);                                            // step 1: change it in memory
        studentRepo.updateFeeBalance(student.getId(), student.getFeeBalance()); // step 2: save it
        return student;
    }
}