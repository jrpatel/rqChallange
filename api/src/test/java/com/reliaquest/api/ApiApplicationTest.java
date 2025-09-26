package com.reliaquest.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.exception.ValidationException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import com.reliaquest.api.service.EmployeeService;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ApiApplicationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private EmployeeService employeeService;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v2/employee";
    }

    @Test
    void getAllEmployees_Success() {
        // Given
        Employee employee = new Employee();
        employee.setId("1");
        employee.setEmployeeName("John Doe");
        employee.setEmployeeSalary(50000);
        employee.setEmployeeAge(30);
        employee.setEmployeeTitle("Developer");
        employee.setEmployeeEmail("john@company.com");

        when(employeeService.getAllEmployees()).thenReturn(Arrays.asList(employee));

        // When
        ResponseEntity<Employee[]> response = restTemplate.getForEntity(getBaseUrl(), Employee[].class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().length);
        assertEquals("John Doe", response.getBody()[0].getEmployeeName());
    }

    @Test
    void createEmployee_InvalidInput_ShouldReturnBadRequest() {
        // Given - invalid employee input (missing required fields)
        EmployeeInput invalidInput = new EmployeeInput();
        when(employeeService.createEmployee(any(EmployeeInput.class)))
                .thenThrow(
                        new ValidationException(
                                "name: Name is required, salary: Salary is required, age: Age is required, title: Title is required"));

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(getBaseUrl(), invalidInput, Map.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createEmployee_InvalidAge_ShouldReturnBadRequest() {
        // Given - employee with invalid age
        EmployeeInput invalidInput = new EmployeeInput();
        invalidInput.setName("John Doe");
        invalidInput.setSalary(50000);
        invalidInput.setAge(15); // Below minimum age of 16
        invalidInput.setTitle("Developer");
        when(employeeService.createEmployee(any(EmployeeInput.class)))
                .thenThrow(new ValidationException("age: Age must be at least 16"));

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(getBaseUrl(), invalidInput, Map.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
