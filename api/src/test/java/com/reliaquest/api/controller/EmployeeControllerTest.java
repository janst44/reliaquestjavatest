package com.reliaquest.api.controller;

import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.model.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Test
    @DisplayName("GET /api/v1/employees returns all employees")
    void getAllEmployees() throws Exception {
        List<Employee> employees = Arrays.asList(
                Employee.builder().id("1").name("Alice").build(),
                Employee.builder().id("2").name("Bob").build()
        );
        Mockito.when(employeeService.getAllEmployees()).thenReturn(employees);
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[1].name").value("Bob"));
    }

    @Test
    @DisplayName("GET /api/v1/employees/{id} returns employee if found")
    void getEmployeeById_found() throws Exception {
        Employee employee = Employee.builder().id("1").name("Alice").build();
        Mockito.when(employeeService.getEmployeeById("1")).thenReturn(employee);
        mockMvc.perform(get("/api/v1/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    @DisplayName("GET /api/v1/employees/{id} returns 404 if not found")
    void getEmployeeById_notFound() throws Exception {
        Mockito.when(employeeService.getEmployeeById("99")).thenThrow(new EmployeeNotFoundException("Not found"));
        mockMvc.perform(get("/api/v1/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Not found"));
    }

    @Test
    @DisplayName("POST /api/v1/employees creates employee")
    void createEmployee() throws Exception {
        CreateEmployeeRequest req = new CreateEmployeeRequest();
        req.setName("Charlie");
        req.setSalary(1000);
        req.setAge(30);
        req.setTitle("Engineer");
        Employee employee = Employee.builder().id("3").name("Charlie").build();
        Mockito.when(employeeService.createEmployee(any(CreateEmployeeRequest.class))).thenReturn(employee);
        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Charlie\",\"salary\":1000,\"age\":30,\"title\":\"Engineer\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Charlie"));
    }

    @Test
    @DisplayName("DELETE /api/v1/employees/{id} returns deleted name if found")
    void deleteEmployeeById_found() throws Exception {
        Mockito.when(employeeService.deleteEmployee("1")).thenReturn("Alice");
        mockMvc.perform(delete("/api/v1/employees/1"))
                .andExpect(status().isOk())
                .andExpect(content().json("\"Alice\""));
    }

    @Test
    @DisplayName("DELETE /api/v1/employees/{id} returns 404 if not found")
    void deleteEmployeeById_notFound() throws Exception {
        Mockito.when(employeeService.deleteEmployee("99")).thenThrow(new EmployeeNotFoundException("Not found"));
        mockMvc.perform(delete("/api/v1/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Not found"));
    }
}