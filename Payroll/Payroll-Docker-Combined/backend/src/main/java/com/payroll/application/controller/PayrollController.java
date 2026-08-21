package com.payroll.application.controller;

import com.payroll.application.dto.ApiResponse;
import com.payroll.application.dto.PayrollResponse;
import com.payroll.application.service.PayrollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payroll")
@Tag(name = "Payroll", description = "Payroll calculation APIs")
public class PayrollController {
    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @GetMapping("/{employeeId}")
    @Operation(summary = "Calculate payroll for an employee")
    public ResponseEntity<ApiResponse<PayrollResponse>> calculatePayroll(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Payroll calculated successfully",
                payrollService.calculatePayroll(employeeId)));
    }
}
