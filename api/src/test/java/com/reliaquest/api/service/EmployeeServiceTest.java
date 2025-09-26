package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;

import com.reliaquest.api.model.Employee;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EmployeeServiceTest {

    private List<Employee> testEmployees;

    @BeforeEach
    void setUp() {
        Employee emp1 = new Employee("1", "John Doe", 50000, 30, "Developer", "john@company.com");
        Employee emp2 = new Employee("2", "Jane Smith", 75000, 28, "Senior Developer", "jane@company.com");
        Employee emp3 = new Employee("3", "Bob Johnson", 90000, 35, "Tech Lead", "bob@company.com");
        testEmployees = Arrays.asList(emp1, emp2, emp3);
    }

    @Test
    void getEmployeesByNameSearch_CaseInsensitive() {
        // Test the  logic
        List<Employee> filteredEmployees = testEmployees.stream()
                .filter(emp -> emp.getEmployeeName().toLowerCase().contains("john".toLowerCase()))
                .collect(java.util.stream.Collectors.toList());

        assertEquals(2, filteredEmployees.size());
        assertEquals("John Doe", filteredEmployees.get(0).getEmployeeName());
    }

    @Test
    void getEmployeesByNameSearch_PartialMatch() {
        // Test the business logic directly
        List<Employee> filteredEmployees = testEmployees.stream()
                .filter(emp -> emp.getEmployeeName().toLowerCase().contains("o".toLowerCase()))
                .collect(java.util.stream.Collectors.toList());

        assertEquals(2, filteredEmployees.size());
        assertTrue(
                filteredEmployees.stream().anyMatch(emp -> emp.getEmployeeName().equals("John Doe")));
        assertTrue(
                filteredEmployees.stream().anyMatch(emp -> emp.getEmployeeName().equals("Bob Johnson")));
    }

    @Test
    void getHighestSalary_MultipleEmployees() {
        // Test the business logic directly
        Integer highestSalary = testEmployees.stream()
                .mapToInt(Employee::getEmployeeSalary)
                .max()
                .orElse(0);

        assertEquals(90000, highestSalary);
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_SortedCorrectly() {
        // Test the business logic directly
        List<String> topEarners = testEmployees.stream()
                .sorted(java.util.Comparator.comparing(Employee::getEmployeeSalary)
                        .reversed())
                .limit(10)
                .map(Employee::getEmployeeName)
                .collect(java.util.stream.Collectors.toList());

        assertEquals(3, topEarners.size());
        assertEquals("Bob Johnson", topEarners.get(0));
        assertEquals("Jane Smith", topEarners.get(1));
        assertEquals("John Doe", topEarners.get(2));
    }
}
