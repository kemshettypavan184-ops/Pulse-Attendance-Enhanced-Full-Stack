package com.pulse.attendance;

import com.pulse.attendance.dto.EmployeeRequest;
import com.pulse.attendance.dto.EmployeeResponse;
import com.pulse.attendance.dto.PageResponse;
import com.pulse.attendance.entity.Employee;
import com.pulse.attendance.enums.EmployeeStatus;
import com.pulse.attendance.exception.DuplicateResourceException;
import com.pulse.attendance.exception.ResourceNotFoundException;
import com.pulse.attendance.repository.EmployeeRepository;
import com.pulse.attendance.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    @InjectMocks private EmployeeService employeeService;

    private EmployeeRequest request;
    private Employee employee;

    @BeforeEach
    void setUp() {
        request = new EmployeeRequest(
                "EMP001", "John Doe", "john@example.com", "1234567890",
                LocalDate.of(1990, 5, 15), LocalDate.of(2020, 1, 1),
                "Engineering", "Software Engineer", EmployeeStatus.ACTIVE,
                "Jane Manager", 100L, 50000.0);

        employee = new Employee();
        employee.setId(1L);
        employee.setEmployeeId("EMP001");
        employee.setFullName("John Doe");
        employee.setEmail("john@example.com");
        employee.setPhoneNumber("1234567890");
        employee.setDepartment("Engineering");
        employee.setDesignation("Software Engineer");
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setReportingManager("Jane Manager");
        employee.setManagerId(100L);
        employee.setSalary(50000.0);
        employee.setDateOfBirth(LocalDate.of(1990, 5, 15));
        employee.setJoinDate(LocalDate.of(2020, 1, 1));
        employee.setCreatedAt(LocalDate.now());
        employee.setUpdatedAt(LocalDate.now());
    }

    @Test
    void createEmployee_success() {
        when(employeeRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(employeeRepository.findByEmployeeId("EMP001")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        EmployeeResponse result = employeeService.createEmployee(request);

        assertNotNull(result);
        assertEquals("EMP001", result.getEmployeeId());
        assertEquals("John Doe", result.getFullName());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void createEmployee_duplicateEmail() {
        when(employeeRepository.findByEmail("john@example.com")).thenReturn(Optional.of(employee));

        assertThrows(DuplicateResourceException.class,
                () -> employeeService.createEmployee(request));

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void createEmployee_duplicateEmployeeId() {
        when(employeeRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(employeeRepository.findByEmployeeId("EMP001")).thenReturn(Optional.of(employee));

        assertThrows(DuplicateResourceException.class,
                () -> employeeService.createEmployee(request));

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void getEmployeeById_success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        EmployeeResponse result = employeeService.getEmployeeById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getFullName());
    }

    @Test
    void getEmployeeById_notFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.getEmployeeById(999L));
    }

    @Test
    void getEmployeeByEmployeeId_success() {
        when(employeeRepository.findByEmployeeId("EMP001")).thenReturn(Optional.of(employee));

        EmployeeResponse result = employeeService.getEmployeeByEmployeeId("EMP001");

        assertEquals("EMP001", result.getEmployeeId());
    }

    @Test
    void getEmployeeByEmployeeId_notFound() {
        when(employeeRepository.findByEmployeeId("EMP999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.getEmployeeByEmployeeId("EMP999"));
    }

    @Test
    void getAllEmployees_success() {
        when(employeeRepository.findAll()).thenReturn(List.of(employee));

        List<EmployeeResponse> result = employeeService.getAllEmployees();

        assertEquals(1, result.size());
        assertEquals("EMP001", result.get(0).getEmployeeId());
    }

    @Test
    void getEmployeesByDepartment_success() {
        when(employeeRepository.findByDepartment("Engineering")).thenReturn(List.of(employee));

        List<EmployeeResponse> result =
                employeeService.getEmployeesByDepartment("Engineering");

        assertEquals(1, result.size());
        assertEquals("Engineering", result.get(0).getDepartment());
    }

    @Test
    void searchEmployees_withFilters() {
        Page<Employee> page = new PageImpl<>(List.of(employee), PageRequest.of(0, 10), 1);
        when(employeeRepository.findAll(
                any(Specification.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        PageResponse<EmployeeResponse> result = employeeService.searchEmployees(
                "John", null, null, "Engineering", "Software",
                EmployeeStatus.ACTIVE,
                PageRequest.of(0, 10, Sort.by("fullName")));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());
        verify(employeeRepository).findAll(
                any(Specification.class), any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    void searchEmployees_emptyResult() {
        Page<Employee> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(employeeRepository.findAll(
                any(Specification.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        PageResponse<EmployeeResponse> result = employeeService.searchEmployees(
                null, null, null, "Unknown", null, null,
                PageRequest.of(0, 10));

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void updateEmployee_success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.findByEmail("john@example.com")).thenReturn(Optional.of(employee));
        when(employeeRepository.findByEmployeeId("EMP001")).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        EmployeeResponse result = employeeService.updateEmployee(1L, request);

        assertNotNull(result);
        assertEquals("John Doe", result.getFullName());
        verify(employeeRepository).save(employee);
    }

    @Test
    void updateEmployee_notFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.updateEmployee(999L, request));

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void updateEmployee_duplicateEmail() {
        Employee other = new Employee();
        other.setId(2L);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.findByEmail("john@example.com")).thenReturn(Optional.of(other));

        assertThrows(DuplicateResourceException.class,
                () -> employeeService.updateEmployee(1L, request));

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void deleteEmployee_success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        employeeService.deleteEmployee(1L);

        verify(employeeRepository).delete(employee);
    }

    @Test
    void deleteEmployee_notFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.deleteEmployee(999L));

        verify(employeeRepository, never()).delete(any(Employee.class));
    }
}
