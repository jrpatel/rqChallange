package com.reliaquest.api.service;

import com.reliaquest.api.exception.ValidationException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import com.reliaquest.api.model.EmployeeResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.*;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

@RequiredArgsConstructor
@Service
@Slf4j
@Validated
public class EmployeeService {

    @Getter
    private final WebClient webClient;

    @Getter
    private final Retry defaultRetrySpec;

    private final Validator validator;

    private record DeleteRequest(String name) {}

    @Value("${employee-v1.api.endpoints.default:}")
    private String defaultEndpoint;

    @Value("${employee-v1.api.endpoints.get-by-id:/{id}}")
    private String getByIdEndpoint;

    @Cacheable(value = "employees", key = "'all'")
    public List<Employee> getAllEmployees() {

        EmployeeResponse<List<Employee>> response = this.webClient
                .get()
                .uri(defaultEndpoint)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<EmployeeResponse<List<Employee>>>() {})
                .retryWhen(defaultRetrySpec)
                .block();

        if (response != null && response.getData() != null) {
            return response.getData();
        }
        return Collections.emptyList();
    }

    @Cacheable(value = "employees", key = "#id")
    public Optional<Employee> getEmployeeById(String id) {
        log.info("Fetching employee by ID: {}", id);
        EmployeeResponse<Employee> response = this.webClient
                .get()
                .uri(getByIdEndpoint, id)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<EmployeeResponse<Employee>>() {})
                .retryWhen(defaultRetrySpec)
                .block();
        return Optional.ofNullable(response != null ? response.getData() : null);
    }

    @Cacheable(value = "employees", key = "'search_' + #searchString")
    public List<Employee> getEmployeesByNameSearch(String searchString) {
        log.info("Searching employees by name: {}", searchString);
        List<Employee> allEmployees = getAllEmployees();
        List<Employee> filteredEmployees = allEmployees.stream()
                .filter(emp -> emp.getEmployeeName().toLowerCase().contains(searchString.toLowerCase()))
                .collect(Collectors.toList());
        log.info("Found {} employees matching search: {}", filteredEmployees.size(), searchString);
        return filteredEmployees;
    }

    @Cacheable(value = "employees", key = "'highestSalary'")
    public Integer getHighestSalary() {
        log.info("Calculating highest salary from cached data");
        List<Employee> employees = getAllEmployees();
        Integer highestSalary =
                employees.stream().mapToInt(Employee::getEmployeeSalary).max().orElse(0);

        log.info("Highest salary found: {}", highestSalary);
        return highestSalary;
    }

    @Cacheable(value = "employees", key = "'top10'")
    public List<String> getTopTenHighestEarningEmployeeNames() {
        log.info("Calculating top 10 highest earners from cached data");
        List<Employee> employees = getAllEmployees();
        List<String> topEarners = employees.stream()
                .sorted(Comparator.comparing(Employee::getEmployeeSalary).reversed())
                .limit(10)
                .map(Employee::getEmployeeName)
                .collect(Collectors.toList());

        log.info("Top 10 highest earners calculated: {}", topEarners.size());
        return topEarners;
    }

    @CacheEvict(value = "employees", allEntries = true)
    public Employee createEmployee(EmployeeInput input) {
        validateEmployeeInput(input);
        log.info("Creating new employee: {}", input.getName());
        EmployeeResponse<Employee> response = this.webClient
                .post()
                .uri("")
                .bodyValue(input)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<EmployeeResponse<Employee>>() {})
                .retryWhen(defaultRetrySpec)
                .block();

        if (response != null && response.getData() != null) {
            log.info("Successfully created employee: {}", response.getData().getEmployeeName());
            return response.getData();
        }
       throw new RuntimeException("Failed to create Employee");
    }

    @CacheEvict(value = "employees", allEntries = true)
    public String deleteEmployeeById(String id) {
        log.info("Deleting employee by ID: {}", id);

        // First get the employee to get the name.
        Employee employee = getEmployeeById(id).orElseThrow(() -> new RuntimeException("Employee Not Found!"));
        DeleteRequest payload = new DeleteRequest(employee.getEmployeeName());

        // Note: delete by name
        EmployeeResponse<Boolean> response = this.webClient
                .method(HttpMethod.DELETE)
                .uri("")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<EmployeeResponse<Boolean>>() {})
                .retryWhen(defaultRetrySpec)
                .block();

        if (response != null && Boolean.TRUE.equals(response.getData())) {
            log.info("Successfully deleted employee: {}", payload.name());
            return payload.name;
        }
        throw  new RuntimeException("Failed to delete employee");
    }

    private void validateEmployeeInput(EmployeeInput input) {
        Set<ConstraintViolation<EmployeeInput>> violations = validator.validate(input);
        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .collect(Collectors.joining(", "));
            throw new ValidationException(errors);
        }
    }
}
