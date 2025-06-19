package com.reliaquest.api.service.impl;

import com.reliaquest.api.dto.Response;
import com.reliaquest.api.exception.EmployeeApiException;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.model.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmployeeServiceImpl implements EmployeeService {
    private final RestTemplate restTemplate;
    private String baseUrl;

    public EmployeeServiceImpl(RestTemplate restTemplate,
                               @Value("${employee.api.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }
    @Retryable(maxAttempts = 5,
            backoff = @Backoff(delay = 10000, multiplier = 2.0, maxDelay = 60000, random = true))
    @Override
    public List<Employee> getAllEmployees() {
        log.info("Fetching all employees");
        try {
            ResponseEntity<Response<List<Employee>>> response = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Response<List<Employee>>>() {}
            );
            return Optional.ofNullable(response.getBody())
                    .map(Response::getData)
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            log.error("Error fetching employees", e);
            throw new EmployeeApiException("Failed to fetch employees", e);
        }
    }

    @Retryable(maxAttempts = 5,
            backoff = @Backoff(delay = 10000, multiplier = 2.0, maxDelay = 60000, random = true))
    @Override
    public List<Employee> searchEmployeesByName(String searchString) {
        log.info("Searching employees with name containing: {}", searchString);
        try {
            List<Employee> allEmployees = getAllEmployees();
            return allEmployees.stream()
                    .filter(emp -> emp.getName().toLowerCase().contains(searchString.toLowerCase()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error searching employees by name", e);
            throw new EmployeeApiException("Failed to search employees", e);
        }
    }

    @Retryable(maxAttempts = 5,
            backoff = @Backoff(delay = 10000, multiplier = 2.0, maxDelay = 60000, random = true))
    @Override
    public Employee getEmployeeById(String id) {
        log.info("Fetching employee with id: {}", id);
        try {
            ResponseEntity<Response<Employee>> response = restTemplate.exchange(
                baseUrl + "/" + id,
                HttpMethod.GET,
                null,
                    new ParameterizedTypeReference<>() {
                    }
            );
            return Optional.ofNullable(response.getBody())
                    .map(Response::getData)
                    .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));
        } catch (HttpClientErrorException.NotFound e) {
            throw new EmployeeNotFoundException("Employee not found with id: " + id);
        } catch (Exception e) {
            log.error("Error fetching employee by id", e);
            throw new EmployeeApiException("Failed to fetch employee", e);
        }
    }

    @Retryable(maxAttempts = 5,
            backoff = @Backoff(delay = 10000, multiplier = 2.0, maxDelay = 60000, random = true))
    @Override
    public Integer getHighestSalary() {
        log.info("Fetching highest salary");
        try {
            return getAllEmployees().stream()
                    .map(Employee::getSalary)
                    .max(Integer::compareTo)
                    .orElse(0);
        } catch (Exception e) {
            log.error("Error fetching highest salary", e);
            throw new EmployeeApiException("Failed to get highest salary", e);
        }
    }

    @Retryable(maxAttempts = 5,
            backoff = @Backoff(delay = 10000, multiplier = 2.0, maxDelay = 60000, random = true))
    @Override
    public List<String> getTopTenHighestEarningEmployeeNames() {
        log.info("Fetching top 10 highest earning employee names");
        try {
            return getAllEmployees().stream()
                    .sorted((e1, e2) -> e2.getSalary().compareTo(e1.getSalary()))
                    .limit(10)
                    .map(Employee::getName)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching top 10 highest earning employees", e);
            throw new EmployeeApiException("Failed to get top 10 highest earning employees", e);
        }
    }

    @Retryable(maxAttempts = 5,
            backoff = @Backoff(delay = 10000, multiplier = 2.0, maxDelay = 60000, random = true))
    @Override
    public Employee createEmployee(CreateEmployeeRequest request) {
        log.info("Creating new employee: {}", request);
        try {
            HttpEntity<CreateEmployeeRequest> requestEntity = new HttpEntity<>(request);
            ResponseEntity<Response<Employee>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                requestEntity,
                    new ParameterizedTypeReference<>() {
                    }
            );
            return Optional.ofNullable(response.getBody())
                    .map(Response::getData)
                    .orElseThrow(() -> new EmployeeApiException("Failed to create employee"));
        } catch (Exception e) {
            log.error("Error creating employee", e);
            throw new EmployeeApiException("Failed to create employee", e);
        }
    }

    @Retryable(maxAttempts = 5,
            backoff = @Backoff(delay = 10000, multiplier = 2.0, maxDelay = 60000, random = true))
    @Override
    public String deleteEmployee(String id) {
        log.info("Deleting employee with id: {}", id);
        try {
            Employee employee = getEmployeeById(id);

            Map<String, String> body = Collections.singletonMap("name", employee.getName());

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body);

            restTemplate.exchange(
                    baseUrl,
                    HttpMethod.DELETE,
                    requestEntity,
                    new ParameterizedTypeReference<Response<Boolean>>() {}
            );

            return employee.getName();
        } catch (EmployeeNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting employee", e);
            throw new EmployeeApiException("Failed to delete employee", e);
        }
    }

}