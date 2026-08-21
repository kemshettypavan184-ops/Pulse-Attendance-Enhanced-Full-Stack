package com.payroll.application.service;

import com.payroll.application.dto.PayrollResponse;
import com.payroll.application.exception.EmployeeNotFoundException;
import com.payroll.application.model.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PayrollService {
    private static final Logger log = LoggerFactory.getLogger(PayrollService.class);
    private final com.payroll.application.repository.EmployeeRepository employeeRepository;

    public PayrollService(com.payroll.application.repository.EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public PayrollResponse calculatePayroll(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(
                        "Employee not found with id: " + employeeId));

        double grossSalary = employee.getBasicSalary() + employee.getAllowances();
        double netSalary = grossSalary - employee.getDeductions();

        log.info("Payroll calculated for employeeId={}", employeeId);
        return new PayrollResponse(employee.getId(), employee.getName(),
                employee.getBasicSalary(), employee.getAllowances(),
                employee.getDeductions(), grossSalary, netSalary);
    }
}
