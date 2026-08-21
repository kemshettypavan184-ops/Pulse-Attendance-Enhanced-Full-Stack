package com.pulse.attendance;

import com.pulse.attendance.dto.EmployeeResponse;
import com.pulse.attendance.dto.LeaveResponse;
import com.pulse.attendance.entity.Employee;
import com.pulse.attendance.entity.Leave;
import com.pulse.attendance.enums.LeaveStatus;
import com.pulse.attendance.repository.EmployeeRepository;
import com.pulse.attendance.repository.LeaveRepository;
import com.pulse.attendance.service.HrAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HrAdminServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private LeaveRepository leaveRepository;
    @Mock private ModelMapper modelMapper;
    @InjectMocks private HrAdminService hrAdminService;

    private Employee employee;
    private Leave leave;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setFullName("John Doe");

        leave = new Leave();
        leave.setId(10L);
        leave.setEmployee(employee);
        leave.setStatus(LeaveStatus.PENDING);
    }

    @Test
    void getAllEmployees_success() {
        EmployeeResponse response = mock(EmployeeResponse.class);
        when(employeeRepository.findAll()).thenReturn(List.of(employee));
        when(modelMapper.map(employee, EmployeeResponse.class)).thenReturn(response);

        List<EmployeeResponse> result = hrAdminService.getAllEmployees();

        assertEquals(1, result.size());
        verify(employeeRepository).findAll();
    }

    @Test
    void getAllLeaveRequests_success() {
        LeaveResponse response = mock(LeaveResponse.class);
        when(leaveRepository.findAll()).thenReturn(List.of(leave));
        when(modelMapper.map(leave, LeaveResponse.class)).thenReturn(response);

        List<LeaveResponse> result = hrAdminService.getAllLeaveRequests();

        assertEquals(1, result.size());
        verify(leaveRepository).findAll();
    }

    @Test
    void getPendingLeaveRequests_success() {
        LeaveResponse response = mock(LeaveResponse.class);
        when(leaveRepository.findByStatus(LeaveStatus.PENDING)).thenReturn(List.of(leave));
        when(modelMapper.map(leave, LeaveResponse.class)).thenReturn(response);

        List<LeaveResponse> result = hrAdminService.getPendingLeaveRequests();

        assertEquals(1, result.size());
        verify(leaveRepository).findByStatus(LeaveStatus.PENDING);
    }

    @Test
    void approveAllPendingLeaves_success() {
        when(leaveRepository.findByStatus(LeaveStatus.PENDING)).thenReturn(List.of(leave));

        hrAdminService.approveAllPendingLeaves("HR Admin");

        assertEquals(LeaveStatus.APPROVED, leave.getStatus());
        assertEquals("HR Admin", leave.getApprovedBy());
        verify(leaveRepository).saveAll(List.of(leave));
    }

    @Test
    void getEmployeeCount_success() {
        when(employeeRepository.count()).thenReturn(5L);

        assertEquals(5L, hrAdminService.getEmployeeCount());
    }

    @Test
    void getPendingLeaveCount_success() {
        when(leaveRepository.findByStatus(LeaveStatus.PENDING))
                .thenReturn(List.of(leave));

        assertEquals(1L, hrAdminService.getPendingLeaveCount());
    }

    @Test
    void getAllEmployees_emptyResult() {
        when(employeeRepository.findAll()).thenReturn(List.of());

        assertTrue(hrAdminService.getAllEmployees().isEmpty());
    }
}
