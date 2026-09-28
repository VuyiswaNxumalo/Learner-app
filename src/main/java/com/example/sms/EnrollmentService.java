package com.example.sms;

public class EnrollmentService {

    private final StudentRepository studentRepo = new StudentRepository();
    private final CourseRepository courseRepo = new CourseRepository();
    private final EnrollmentRepository enrollmentRepo = new EnrollmentRepository();

    /**
     * Enrolls a student in a course and adds the course fee to their balance.
     * Throws IllegalArgumentException with a readable message if anything is wrong.
     */
    public Student enroll(String studentId, int courseId) {
        Student student = studentRepo.findByStudentId(studentId);
        if (student == null) {
            throw new IllegalArgumentException("No student found with ID " + studentId);
        }

        Course course = courseRepo.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("No course found with ID " + courseId);
        }

        boolean enrolled = enrollmentRepo.enroll(student.getId(), course.getId());
        if (!enrolled) {
            throw new IllegalArgumentException(
                    student.getName() + " is already enrolled in " + course.getCode());
        }

        // only reached on a first-time enrollment, so the fee is charged once
        student.addFee(course.getFee());
        studentRepo.updateFeeBalance(student.getId(), student.getFeeBalance());
        return student;
    }
}