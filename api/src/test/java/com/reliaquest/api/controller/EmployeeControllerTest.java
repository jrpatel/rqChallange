package com.reliaquest.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import com.reliaquest.api.service.EmployeeService;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private Employee testEmployee;
    private List<Employee> testEmployees;
    private EmployeeInput testEmployeeInput;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee();
        testEmployee.setId("1");
        testEmployee.setEmployeeName("John Doe");
        testEmployee.setEmployeeSalary(50000);
        testEmployee.setEmployeeAge(30);
        testEmployee.setEmployeeTitle("Developer");
        testEmployee.setEmployeeEmail("john@company.com");

        testEmployees = Arrays.asList(testEmployee);

        testEmployeeInput = new EmployeeInput();
        testEmployeeInput.setName("Jane Doe");
        testEmployeeInput.setSalary(60000);
        testEmployeeInput.setAge(25);
        testEmployeeInput.setTitle("Senior Developer");
    }

    @Test
    void getAllEmployees_Success() {
        // Given
        when(employeeService.getAllEmployees()).thenReturn(testEmployees);

        // When
        ResponseEntity<List<Employee>> response = employeeController.getAllEmployees();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("John Doe", response.getBody().get(0).getEmployeeName());
        verify(employeeService).getAllEmployees();
    }

    @Test
    void getEmployeesByNameSearch_Success() {
        // Given
        when(employeeService.getEmployeesByNameSearch("John")).thenReturn(testEmployees);

        // When
        ResponseEntity<List<Employee>> response = employeeController.getEmployeesByNameSearch("John");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(employeeService).getEmployeesByNameSearch("John");
    }

    @Test
    void getEmployeeById_Success() {
        // Given
        when(employeeService.getEmployeeById("1")).thenReturn(Optional.ofNullable(testEmployee));

        // When
        ResponseEntity<Employee> response = employeeController.getEmployeeById("1");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John Doe", response.getBody().getEmployeeName());
        verify(employeeService).getEmployeeById("1");
    }

    @Test
    void getHighestSalaryOfEmployees_Success() {
        // Given
        when(employeeService.getHighestSalary()).thenReturn(100000);

        // When
        ResponseEntity<Integer> response = employeeController.getHighestSalaryOfEmployees();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100000, response.getBody());
        verify(employeeService).getHighestSalary();
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_Success() {
        // Given
        List<String> topEarners = Arrays.asList("John Doe", "Jane Smith");
        when(employeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(topEarners);

        // When
        ResponseEntity<List<String>> response = employeeController.getTopTenHighestEarningEmployeeNames();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("John Doe", response.getBody().get(0));
        verify(employeeService).getTopTenHighestEarningEmployeeNames();
    }

    @Test
    void createEmployee_Success() {
        // Given
        when(employeeService.createEmployee(testEmployeeInput)).thenReturn(testEmployee);

        // When
        ResponseEntity<Employee> response = employeeController.createEmployee(testEmployeeInput);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John Doe", response.getBody().getEmployeeName());
        verify(employeeService).createEmployee(testEmployeeInput);
    }

    @Test
    void deleteEmployeeById_Success() {
        // Given
        when(employeeService.deleteEmployeeById("1")).thenReturn("John Doe");

        // When
        ResponseEntity<String> response = employeeController.deleteEmployeeById("1");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John Doe", response.getBody());
        verify(employeeService).deleteEmployeeById("1");
    }
}
