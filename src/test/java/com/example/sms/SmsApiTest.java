package com.example.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SmsApiTest {

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();

    private Javalin app;
    private String baseUrl;

    @BeforeEach
    void startServerWithFreshDatabase(@TempDir Path tempDir) {
        System.setProperty("sms.db.url", "jdbc:sqlite:" + tempDir.resolve("test.db"));
        new StudentRepository().createTable();
        new CourseRepository().createTable();
        new EnrollmentRepository().createTable();

        app = Javalin.create();
        new SmsApi().register(app);
        app.start(0);                                  // port 0 = pick any free port
        baseUrl = "http://localhost:" + app.port();
    }

    @AfterEach
    void stopServer() {
        app.stop();
        System.clearProperty("sms.db.url");
    }

    // ---- helpers: send a request and return the response ----

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path)).GET().build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static final String CS101 = "{\"code\":\"CS101\",\"title\":\"Intro to CS\",\"fee\":150}";
    private static final String JOHN = "{\"name\":\"John Smith\",\"age\":20,\"email\":\"john@example.com\"}";
    private static final String JANE = "{\"name\":\"Jane Doe\",\"age\":19,\"email\":\"jane@example.com\"}";

    private static String enrollBody(int courseId) {
        return "{\"courseId\":" + courseId + "}";
    }

    private static String payBody(double amount) {
        return "{\"amount\":" + amount + "}";
    }

    // a student who owes $150 (registered and enrolled in CS101)
    private void johnOwes150() throws Exception {
        post("/api/courses", CS101);
        post("/api/students", JOHN);
        post("/api/students/STU00001/enrollments", enrollBody(1));
    }

    // ---- courses ----

    @Test
    void addingACourseReturns201AndTheCourse() throws Exception {
        HttpResponse<String> response = post("/api/courses", CS101);

        assertEquals(201, response.statusCode());
        JsonNode body = json.readTree(response.body());
        assertEquals("CS101", body.get("code").asText());
        assertTrue(body.get("id").asInt() > 0);
    }

    @Test
    void duplicateCourseCodeReturns409() throws Exception {
        post("/api/courses", CS101);

        HttpResponse<String> response = post("/api/courses", CS101);

        assertEquals(409, response.statusCode());
        assertTrue(json.readTree(response.body()).get("error").asText().contains("CS101"));
    }

    @Test
    void courseWithoutFeeReturns400() throws Exception {
        HttpResponse<String> response = post("/api/courses", "{\"code\":\"ENG105\",\"title\":\"English\"}");

        assertEquals(400, response.statusCode());
        assertEquals("fee is required", json.readTree(response.body()).get("error").asText());
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        HttpResponse<String> response = post("/api/courses", "hello");

        assertEquals(400, response.statusCode());
        assertEquals("request body must be valid JSON", json.readTree(response.body()).get("error").asText());
    }

    // ---- students ----

    @Test
    void registeringAStudentReturns201WithAGeneratedId() throws Exception {
        HttpResponse<String> response = post("/api/students", JOHN);

        assertEquals(201, response.statusCode());
        assertEquals("STU00001", json.readTree(response.body()).get("studentId").asText());
    }

    @Test
    void studentWithBlankNameReturns400() throws Exception {
        HttpResponse<String> response = post("/api/students", "{\"name\":\"\",\"age\":20}");

        assertEquals(400, response.statusCode());
        assertEquals("name is required", json.readTree(response.body()).get("error").asText());
    }

    @Test
    void searchFindsStudentsByName() throws Exception {
        post("/api/students", JOHN);
        post("/api/students", JANE);

        HttpResponse<String> response = get("/api/students?search=jane");

        assertEquals(200, response.statusCode());
        JsonNode list = json.readTree(response.body());
        assertEquals(1, list.size());
        assertEquals("Jane Doe", list.get(0).get("name").asText());
    }

    @Test
    void statusShowsAStudentWithNoCourses() throws Exception {
        post("/api/students", JOHN);

        HttpResponse<String> response = get("/api/students/STU00001");

        assertEquals(200, response.statusCode());
        JsonNode body = json.readTree(response.body());
        assertEquals("John Smith", body.get("name").asText());
        assertEquals(0, body.get("courses").size());
    }

    @Test
    void statusForAnUnknownStudentReturns404() throws Exception {
        HttpResponse<String> response = get("/api/students/STU99999");

        assertEquals(404, response.statusCode());
    }

    // ---- enrolling ----

    @Test
    void enrollingChargesTheFeeAndShowsTheCourse() throws Exception {
        post("/api/courses", CS101);        // id 1, fee 150
        post("/api/students", JOHN);        // STU00001

        HttpResponse<String> response = post("/api/students/STU00001/enrollments", enrollBody(1));

        assertEquals(201, response.statusCode());
        JsonNode body = json.readTree(response.body());
        assertEquals(150.0, body.get("feeBalance").asDouble(), 0.001);
        assertEquals(1, body.get("courses").size());
        assertEquals("CS101", body.get("courses").get(0).get("code").asText());
    }

    @Test
    void duplicateEnrollmentReturns409AndIsChargedOnce() throws Exception {
        johnOwes150();

        HttpResponse<String> second = post("/api/students/STU00001/enrollments", enrollBody(1));

        assertEquals(409, second.statusCode());
        assertTrue(json.readTree(second.body()).get("error").asText().contains("already enrolled"));

        JsonNode status = json.readTree(get("/api/students/STU00001").body());
        assertEquals(150.0, status.get("feeBalance").asDouble(), 0.001);   // one fee, not two
    }

    @Test
    void enrollingAnUnknownStudentReturns404() throws Exception {
        post("/api/courses", CS101);

        HttpResponse<String> response = post("/api/students/STU99999/enrollments", enrollBody(1));

        assertEquals(404, response.statusCode());
    }

    @Test
    void enrollingInAnUnknownCourseReturns404AndChargesNothing() throws Exception {
        post("/api/students", JOHN);

        HttpResponse<String> response = post("/api/students/STU00001/enrollments", enrollBody(999));

        assertEquals(404, response.statusCode());
        JsonNode status = json.readTree(get("/api/students/STU00001").body());
        assertEquals(0.0, status.get("feeBalance").asDouble(), 0.001);
    }

    @Test
    void enrollmentWithoutACourseIdReturns400() throws Exception {
        post("/api/students", JOHN);

        HttpResponse<String> response = post("/api/students/STU00001/enrollments", "{}");

        assertEquals(400, response.statusCode());
        assertEquals("courseId is required", json.readTree(response.body()).get("error").asText());
    }

    // ---- payments ----

    @Test
    void partialPaymentReducesTheBalance() throws Exception {
        johnOwes150();

        HttpResponse<String> response = post("/api/students/STU00001/payments", payBody(50));

        assertEquals(200, response.statusCode());
        assertEquals(100.0, json.readTree(response.body()).get("feeBalance").asDouble(), 0.001);
    }

    @Test
    void payingMoreThanOwedReturns409AndBalanceIsUnchanged() throws Exception {
        johnOwes150();

        HttpResponse<String> response = post("/api/students/STU00001/payments", payBody(500));

        assertEquals(409, response.statusCode());
        JsonNode status = json.readTree(get("/api/students/STU00001").body());
        assertEquals(150.0, status.get("feeBalance").asDouble(), 0.001);
    }

    @Test
    void zeroPaymentReturns400() throws Exception {
        johnOwes150();

        HttpResponse<String> response = post("/api/students/STU00001/payments", payBody(0));

        assertEquals(400, response.statusCode());
        assertEquals("Payment must be more than zero.", json.readTree(response.body()).get("error").asText());
    }

    @Test
    void payingWhenNothingIsOwedReturns409() throws Exception {
        post("/api/students", JOHN);    // registered, never enrolled, owes nothing

        HttpResponse<String> response = post("/api/students/STU00001/payments", payBody(10));

        assertEquals(409, response.statusCode());
    }

    @Test
    void paymentForAnUnknownStudentReturns404() throws Exception {
        HttpResponse<String> response = post("/api/students/STU99999/payments", payBody(10));

        assertEquals(404, response.statusCode());
    }

    @Test
    void paymentWithoutAnAmountReturns400() throws Exception {
        post("/api/students", JOHN);

        HttpResponse<String> response = post("/api/students/STU00001/payments", "{}");

        assertEquals(400, response.statusCode());
        assertEquals("amount is required", json.readTree(response.body()).get("error").asText());
    }
}