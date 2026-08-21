package com.pulse.attendance;

import com.pulse.attendance.dto.AttendanceResponse;
import com.pulse.attendance.dto.LeaveResponse;
import com.pulse.attendance.entity.Attendance;
import com.pulse.attendance.entity.Employee;
import com.pulse.attendance.entity.Leave;
import com.pulse.attendance.enums.LeaveStatus;
import com.pulse.attendance.repository.AttendanceRepository;
import com.pulse.attendance.repository.EmployeeRepository;
import com.pulse.attendance.repository.LeaveRepository;
import com.pulse.attendance.service.ManagerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private AttendanceRepository attendanceRepository;
    @Mock private LeaveRepository leaveRepository;
    @Mock private ModelMapper modelMapper;
    @InjectMocks private ManagerService managerService;

    private Employee employee;
    private Attendance attendance;
    private Leave leave;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setFullName("John Doe");
        employee.setReportingManager("Jane Manager");

        attendance = new Attendance();
        attendance.setId(10L);
        attendance.setEmployee(employee);

        leave = new Leave();
        leave.setId(20L);
        leave.setEmployee(employee);
        leave.setStatus(LeaveStatus.PENDING);
    }

    @Test
    void getTeamAttendance_success() {
        AttendanceResponse response = mock(AttendanceResponse.class);
        when(employeeRepository.findAll()).thenReturn(List.of(employee));
        when(attendanceRepository.findByEmployeeIdAndAttendanceDateBetween(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(attendance));
        when(modelMapper.map(attendance, AttendanceResponse.class)).thenReturn(response);

        List<AttendanceResponse> result = managerService.getTeamAttendance(
                "Jane Manager", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));

        assertEquals(1, result.size());
    }

    @Test
    void getTeamAttendance_noTeamMembers() {
        when(employeeRepository.findAll()).thenReturn(List.of());

        List<AttendanceResponse> result = managerService.getTeamAttendance(
                "Unknown Manager", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));

        assertTrue(result.isEmpty());
        verifyNoInteractions(attendanceRepository);
    }

    @Test
    void getTeamLeaveRequests_success() {
        LeaveResponse response = mock(LeaveResponse.class);
        when(employeeRepository.findAll()).thenReturn(List.of(employee));
        when(leaveRepository.findByEmployeeAndStatus(employee, LeaveStatus.PENDING))
                .thenReturn(List.of(leave));
        when(modelMapper.map(leave, LeaveResponse.class)).thenReturn(response);

        List<LeaveResponse> result =
                managerService.getTeamLeaveRequests("Jane Manager");

        assertEquals(1, result.size());
    }

    @Test
    void approveLeave_success() {
        when(leaveRepository.findById(20L)).thenReturn(Optional.of(leave));

        managerService.approveLeave(20L, "Jane Manager");

        assertEquals(LeaveStatus.APPROVED, leave.getStatus());
        assertEquals("Jane Manager", leave.getApprovedBy());
        verify(leaveRepository).save(leave);
    }

    @Test
    void approveLeave_notFound() {
        when(leaveRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> managerService.approveLeave(999L, "Jane Manager"));

        verify(leaveRepository, never()).save(any(Leave.class));
    }

    @Test
    void rejectLeave_success() {
        when(leaveRepository.findById(20L)).thenReturn(Optional.of(leave));

        managerService.rejectLeave(20L);

        assertEquals(LeaveStatus.REJECTED, leave.getStatus());
        verify(leaveRepository).save(leave);
    }

    @Test
    void rejectLeave_notFound() {
        when(leaveRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> managerService.rejectLeave(999L));

        verify(leaveRepository, never()).save(any(Leave.class));
    }
}

