package com.reliaquest.api;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;



@SpringBootTest
@AutoConfigureMockMvc
class ApiApplicationTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private MockMvc mockMvc;

    private static final String EMPLOYEE_JOHN_DOE = """
    {
      "id": "1",
      "employee_name": "John Doe",
      "employee_salary": 100000,
      "employee_age": 33,
      "employee_title": "Software Dev",
      "employee_email": "john.doe@ddls.com"
    }
    """;
    private static final String EMPLOYEE_JANE_SMITH = """
    {
      "id": "2",
      "employee_name": "Jane Smith",
      "employee_salary": 120000,
      "employee_age": 29,
      "employee_title": "Product Manager",
      "employee_email": "jane.smith@ddls.com"
    }
    """;
    private static final String EMPLOYEE_ALICE_JOHNSON = """
    {
      "id": "3",
      "employee_name": "Alice Johnson",
      "employee_salary": 90000,
      "employee_age": 35,
      "employee_title": "Designer",
      "employee_email": "alice.johnson@ddls.com"
    }
    """;
    private static final String EMPLOYEE_BOB_BROWN = """
    {
      "id": "4",
      "employee_name": "Bob Brown",
      "employee_salary": 110000,
      "employee_age": 40,
      "employee_title": "Architect",
      "employee_email": "bob.brown@ddls.com"
    }
    """;
    private static final String EMPLOYEE_CHARLIE_GREEN = """
    {
      "id": "5",
      "employee_name": "Charlie Green",
      "employee_salary": 95000,
      "employee_age": 28,
      "employee_title": "QA Engineer",
      "employee_email": "charlie.green@ddls.com"
    }
    """;
    private static final String ALL_EMPLOYEES_LIST = """
    {
      "data": [
        %s
      ]
    }
    """.formatted(EMPLOYEE_JOHN_DOE);
    private static final String TWO_EMPLOYEES_LIST = """
    {
      "data": [
        %s,
        %s
      ]
    }
    """.formatted(EMPLOYEE_JOHN_DOE, EMPLOYEE_JANE_SMITH);
    private static final String FOUR_EMPLOYEES_LIST = """
    {
      "data": [
        %s,
        %s,
        %s,
        %s
      ]
    }
    """.formatted(EMPLOYEE_JOHN_DOE, EMPLOYEE_JANE_SMITH, EMPLOYEE_ALICE_JOHNSON, EMPLOYEE_BOB_BROWN);
    private static final String GET_EMPLOYEE_RESPONSE = """
    {
      "data": %s
    }
    """.formatted(EMPLOYEE_JOHN_DOE);
    private static final String CREATE_EMPLOYEE_REQUEST = """
    {
      "name": "Charlie Green",
      "salary": 95000,
      "age": 28,
      "title": "QA Engineer",
      "email": "charlie.green@ddls.com"
    }
    """;
    private static final String CREATE_EMPLOYEE_RESPONSE = """
    {
      "data": %s
    }
    """.formatted(EMPLOYEE_CHARLIE_GREEN);
    private static final String DELETE_EMPLOYEE_RESPONSE = """
    {
      "data": true
    }
    """;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        String baseUrl = String.format("http://localhost:%s/api/v1/employee", mockWebServer.getPort());
        registry.add("employee.api.base-url", () -> baseUrl);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void getAllEmployees_ShouldReturnOkAndData() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody(ALL_EMPLOYEES_LIST)
                .addHeader("Content-Type", "application/json"));

        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[0].salary").value(100000));
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void searchEmployeesByName_ShouldReturnFilteredEmployees() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody(TWO_EMPLOYEES_LIST)
                .addHeader("Content-Type", "application/json"));

        String searchTerm = "Jane";

        mockMvc.perform(get("/api/v1/employees/search/Jane"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Jane Smith"))
                .andExpect(jsonPath("$[0].salary").value(120000));
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void getEmployeeById_ShouldReturnOkAndEmployee() throws Exception {
        String employeeId = "1";
        mockWebServer.enqueue(new MockResponse()
                .setBody(GET_EMPLOYEE_RESPONSE)
                .addHeader("Content-Type", "application/json"));

        mockMvc.perform(get("/api/v1/employees/" + employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(employeeId))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.salary").value(100000))
                .andExpect(jsonPath("$.age").value(33))
                .andExpect(jsonPath("$.title").value("Software Dev"))
                .andExpect(jsonPath("$.email").value("john.doe@ddls.com"));
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void getHighestSalary_ShouldReturnMaxSalary() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody(FOUR_EMPLOYEES_LIST)
                .addHeader("Content-Type", "application/json"));

        mockMvc.perform(get("/api/v1/employees/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(120000));
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void getTopTenHighestEarningEmployeeNames_ShouldReturnTopNames() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody(FOUR_EMPLOYEES_LIST)
                .addHeader("Content-Type", "application/json"));

        mockMvc.perform(get("/api/v1/employees/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0]").value("Jane Smith"))
                .andExpect(jsonPath("$[1]").value("Bob Brown"))
                .andExpect(jsonPath("$[2]").value("John Doe"))
                .andExpect(jsonPath("$[3]").value("Alice Johnson"));
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void createEmployee_ShouldReturnCreatedEmployee() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody(CREATE_EMPLOYEE_RESPONSE)
                .addHeader("Content-Type", "application/json"));

        mockMvc.perform(post("/api/v1/employees")
                        .contentType("application/json")
                        .content(CREATE_EMPLOYEE_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("5"))
                .andExpect(jsonPath("$.name").value("Charlie Green"))
                .andExpect(jsonPath("$.salary").value(95000))
                .andExpect(jsonPath("$.age").value(28))
                .andExpect(jsonPath("$.title").value("QA Engineer"))
                .andExpect(jsonPath("$.email").value("charlie.green@ddls.com"));
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void deleteEmployee_ShouldReturnDeletedEmployeeName() throws Exception {
        String employeeId = "1";
        mockWebServer.enqueue(new MockResponse()
                .setBody(GET_EMPLOYEE_RESPONSE)
                .addHeader("Content-Type", "application/json"));

        mockWebServer.enqueue(new MockResponse()
                .setBody(DELETE_EMPLOYEE_RESPONSE)
                .addHeader("Content-Type", "application/json"));

        mockMvc.perform(delete("/api/v1/employees/" + employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("John Doe"));
    }

}
