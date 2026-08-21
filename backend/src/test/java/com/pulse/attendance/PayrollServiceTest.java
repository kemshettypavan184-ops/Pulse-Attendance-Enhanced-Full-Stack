package com.pulse.attendance;

import com.pulse.attendance.dto.PayrollResponse;
import com.pulse.attendance.entity.Employee;
import com.pulse.attendance.entity.PayrollRecord;
import com.pulse.attendance.exception.ResourceNotFoundException;
import com.pulse.attendance.repository.EmployeeRepository;
import com.pulse.attendance.repository.PayrollRepository;
import com.pulse.attendance.service.PayrollService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayrollServiceTest {

    @Mock private PayrollRepository payrollRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private ModelMapper modelMapper;
    @InjectMocks private PayrollService payrollService;

    private Employee employee;
    private PayrollRecord payroll;
    private PayrollResponse response;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setFullName("John Doe");
        employee.setSalary(50000.0);

        payroll = new PayrollRecord();
        payroll.setId(10L);
        payroll.setEmployee(employee);
        payroll.setMonth(YearMonth.of(2026, 8));

        response = mock(PayrollResponse.class);
    }

    @Test
    void generatePayroll_success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(payrollRepository.save(any(PayrollRecord.class))).thenReturn(payroll);
        when(modelMapper.map(payroll, PayrollResponse.class)).thenReturn(response);

        PayrollResponse result =
                payrollService.generatePayroll(1L, YearMonth.of(2026, 8), 20);

        assertNotNull(result);
        verify(payrollRepository).save(any(PayrollRecord.class));
    }

    @Test
    void generatePayroll_employeeNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> payrollService.generatePayroll(
                        999L, YearMonth.of(2026, 8), 20));

        verify(payrollRepository, never()).save(any(PayrollRecord.class));
    }

    @Test
    void getPayrollById_success() {
        when(payrollRepository.findById(10L)).thenReturn(Optional.of(payroll));
        when(modelMapper.map(payroll, PayrollResponse.class)).thenReturn(response);

        assertNotNull(payrollService.getPayrollById(10L));
    }

    @Test
    void getPayrollById_notFound() {
        when(payrollRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> payrollService.getPayrollById(999L));
    }

    @Test
    void getPayrollByEmployeeAndMonth_success() {
        YearMonth month = YearMonth.of(2026, 8);
        when(payrollRepository.findByEmployeeIdAndMonth(1L, month))
                .thenReturn(Optional.of(payroll));
        when(modelMapper.map(payroll, PayrollResponse.class)).thenReturn(response);

        assertNotNull(payrollService.getPayrollByEmployeeAndMonth(1L, month));
    }

    @Test
    void getPayrollByEmployeeAndMonth_notFound() {
        YearMonth month = YearMonth.of(2026, 8);
        when(payrollRepository.findByEmployeeIdAndMonth(999L, month))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> payrollService.getPayrollByEmployeeAndMonth(999L, month));
    }

    @Test
    void getPayrollByEmployeeId_success() {
        when(payrollRepository.findByEmployeeId(1L)).thenReturn(List.of(payroll));
        when(modelMapper.map(payroll, PayrollResponse.class)).thenReturn(response);

        assertEquals(1, payrollService.getPayrollByEmployeeId(1L).size());
    }

    @Test
    void getPayrollByMonth_success() {
        YearMonth month = YearMonth.of(2026, 8);
        when(payrollRepository.findByMonth(month)).thenReturn(List.of(payroll));
        when(modelMapper.map(payroll, PayrollResponse.class)).thenReturn(response);

        assertEquals(1, payrollService.getPayrollByMonth(month).size());
    }

    @Test
    void getAllPayroll_success() {
        when(payrollRepository.findAll()).thenReturn(List.of(payroll));
        when(modelMapper.map(payroll, PayrollResponse.class)).thenReturn(response);

        assertEquals(1, payrollService.getAllPayroll().size());
    }

    @Test
    void deletePayroll_success() {
        when(payrollRepository.findById(10L)).thenReturn(Optional.of(payroll));

        payrollService.deletePayroll(10L);

        verify(payrollRepository).delete(payroll);
    }

    @Test
    void deletePayroll_notFound() {
        when(payrollRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> payrollService.deletePayroll(999L));

        verify(payrollRepository, never()).delete(any(PayrollRecord.class));
    }
}
