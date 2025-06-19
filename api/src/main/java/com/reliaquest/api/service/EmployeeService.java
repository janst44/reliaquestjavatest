package com.reliaquest.api.service;

import com.reliaquest.api.model.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;

import java.util.List;

public interface EmployeeService {
    List<Employee> getAllEmployees();

    List<Employee> searchEmployeesByName(String searchString);

    Employee getEmployeeById(String id);

    Integer getHighestSalary();

    List<String> getTopTenHighestEarningEmployeeNames();

    Employee createEmployee(CreateEmployeeRequest request);

    String deleteEmployee(String id);
}