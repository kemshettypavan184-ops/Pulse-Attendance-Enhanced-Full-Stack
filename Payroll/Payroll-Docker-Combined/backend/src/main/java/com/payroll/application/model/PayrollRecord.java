package com.payroll.application.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "payroll_records")
public class PayrollRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate payrollDate;
    private double grossSalary;
    private double netSalary;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    public PayrollRecord() {}

    public PayrollRecord(Employee employee, LocalDate payrollDate,
                         double grossSalary, double netSalary) {
        this.employee = employee;
        this.payrollDate = payrollDate;
        this.grossSalary = grossSalary;
        this.netSalary = netSalary;
    }

    public Long getId() { return id; }
    public LocalDate getPayrollDate() { return payrollDate; }
    public double getGrossSalary() { return grossSalary; }
    public double getNetSalary() { return netSalary; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
}
