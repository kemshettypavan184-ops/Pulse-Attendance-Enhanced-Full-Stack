package com.payroll.application.controller;

import com.payroll.application.dto.*;
import com.payroll.application.model.Employee;
import com.payroll.application.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/employees")
@Tag(name = "Employees", description = "Employee management and advanced search APIs")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    @Operation(summary = "Advanced employee search with filters, sorting and pagination")
    public ResponseEntity<ApiResponse<Page<Employee>>> searchEmployees(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Double minBasicSalary,
            @RequestParam(required = false) Double maxBasicSalary,
            @RequestParam(required = false) Double salary,
            @Parameter(description = "Zero-based page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size, maximum 100") @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        EmployeeSearchRequest request = new EmployeeSearchRequest();
        request.setName(name);
        request.setEmail(email);
        request.setDepartment(department);
        request.setMinBasicSalary(minBasicSalary);
        request.setMaxBasicSalary(maxBasicSalary);
        request.setSalary(salary);

        return ResponseEntity.ok(new ApiResponse<>(true, "Employees retrieved successfully",
                employeeService.search(request, page, size, sortBy, direction)));
    }


    /* NEW GET API - Get all employees */
    @GetMapping("/all")
    @Operation(summary = "Get all employees")
    public ResponseEntity<ApiResponse<List<Employee>>> getAllEmployees() {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "All employees retrieved successfully",
                         employeeService.getAllEmployees()
                )
        );
    }


    @GetMapping("/{id}")
    @Operation(summary = "Get an employee by ID")
    public ResponseEntity<ApiResponse<Employee>> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Employee retrieved successfully",
                employeeService.getEmployeeByIdOrThrow(id)));
    }
    @PostMapping
    @Operation(summary = "Create an employee")
    public ResponseEntity<ApiResponse<Employee>> createEmployee(
            @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Employee created successfully",
                        employeeService.createEmployee(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an employee")
    public ResponseEntity<ApiResponse<Employee>> updateEmployee(
            @PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Employee updated successfully",
                employeeService.updateEmployee(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an employee")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Employee deleted successfully", null));
    }
}
