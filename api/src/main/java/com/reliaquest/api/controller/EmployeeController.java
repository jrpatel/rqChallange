package com.reliaquest.api.controller;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import com.reliaquest.api.service.EmployeeService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/employee")
@Slf4j
@RequiredArgsConstructor
public class EmployeeController implements IEmployeeController<Employee, EmployeeInput> {

    @Autowired
    private EmployeeService employeeService;

    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {

        List<Employee> response = employeeService.getAllEmployees();
        log.debug("employees {}", response.size());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(String searchString) {
        log.info("Received request to search employees by name: {}", searchString);
        if (searchString == null || searchString.trim().isEmpty()) {
            log.warn("Empty search string provided");
            return ResponseEntity.badRequest().build();
        }
        List<Employee> employees = employeeService.getEmployeesByNameSearch(searchString.trim());
        log.info("Found {} employees matching search: {}", employees.size(), searchString);
        return ResponseEntity.ok(employees);
    }

    @Override
    public ResponseEntity<Employee> getEmployeeById(String id) {
        log.info("Received request to get employee by ID: {}", id);
        if (id == null || id.trim().isEmpty()) {
            log.warn("Empty ID provided");
            return ResponseEntity.badRequest().build();
        }
        Optional<Employee> employee = employeeService.getEmployeeById(id.trim());
        return employee.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {

        log.info("Received request to get highest salary");
        Integer highestSalary = employeeService.getHighestSalary();
        return ResponseEntity.ok(highestSalary);
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        log.info("Received request to get top 10 highest earning employee names");
        List<String> topEarners = employeeService.getTopTenHighestEarningEmployeeNames();
        return ResponseEntity.ok(topEarners);
    }

    @Override
    public ResponseEntity<Employee> createEmployee(EmployeeInput employeeInput) {
        log.info("Received request to create employee");

        Employee createdEmployee = employeeService.createEmployee(employeeInput);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
        log.info("Received request to delete employee by ID: {}", id);
        if (id == null || id.trim().isEmpty()) {
            log.warn("Empty ID provided for deletion");
            return ResponseEntity.badRequest().build();
        }
        String deletedEmployeeName = employeeService.deleteEmployeeById(id.trim());
        return ResponseEntity.ok(deletedEmployeeName);
    }
}
