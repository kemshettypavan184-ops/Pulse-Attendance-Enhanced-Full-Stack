package com.pulse.attendance;

import com.pulse.attendance.dto.LeaveRequest;
import com.pulse.attendance.dto.LeaveResponse;
import com.pulse.attendance.entity.Employee;
import com.pulse.attendance.entity.Leave;
import com.pulse.attendance.enums.EmployeeStatus;
import com.pulse.attendance.enums.LeaveStatus;
import com.pulse.attendance.enums.LeaveType;
import com.pulse.attendance.exception.ResourceNotFoundException;
import com.pulse.attendance.repository.EmployeeRepository;
import com.pulse.attendance.repository.LeaveRepository;
import com.pulse.attendance.service.LeaveService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock private LeaveRepository leaveRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private ModelMapper modelMapper;
    @InjectMocks private LeaveService leaveService;

    private Employee employee;
    private LeaveRequest request;
    private Leave leave;
    private LeaveResponse response;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setEmployeeId("EMP001");
        employee.setFullName("John Doe");
        employee.setStatus(EmployeeStatus.ACTIVE);

        request = new LeaveRequest(
                1L, LeaveType.CASUAL_LEAVE,
                LocalDate.of(2026, 8, 21),
                LocalDate.of(2026, 8, 25),
                5, "Personal reasons",
                LeaveStatus.PENDING, "Waiting for approval");

        leave = new Leave();
        leave.setId(10L);
        leave.setEmployee(employee);
        leave.setLeaveType(LeaveType.CASUAL_LEAVE);
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setNumberOfDays(5);
        leave.setReason("Personal reasons");
        leave.setStatus(LeaveStatus.PENDING);

        response = mock(LeaveResponse.class);
        when(response.getEmployeeId()).thenReturn(1L);
        when(response.getEmployeeName()).thenReturn("John Doe");
    }

    @Test
    void applyLeave_success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(modelMapper.map(request, Leave.class)).thenReturn(leave);
        when(leaveRepository.save(any(Leave.class))).thenReturn(leave);
        when(modelMapper.map(leave, LeaveResponse.class)).thenReturn(response);

        LeaveResponse result = leaveService.applyLeave(request);

        assertNotNull(result);
        assertEquals(1L, result.getEmployeeId());
        assertEquals("John Doe", result.getEmployeeName());
        verify(leaveRepository).save(leave);
    }

    @Test
    void applyLeave_employeeNotFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leaveService.applyLeave(request));

        verify(leaveRepository, never()).save(any(Leave.class));
    }

    @Test
    void getLeaveById_success() {
        when(leaveRepository.findById(10L)).thenReturn(Optional.of(leave));
        when(modelMapper.map(leave, LeaveResponse.class)).thenReturn(response);

        LeaveResponse result = leaveService.getLeaveById(10L);

        assertNotNull(result);
        assertEquals("John Doe", result.getEmployeeName());
    }

    @Test
    void getLeaveById_notFound() {
        when(leaveRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leaveService.getLeaveById(999L));
    }

    @Test
    void getLeaveByEmployeeAndDateRange_success() {
        when(leaveRepository.findByEmployeeIdAndStartDateBetween(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(leave));
        when(modelMapper.map(leave, LeaveResponse.class)).thenReturn(response);

        List<LeaveResponse> result =
                leaveService.getLeaveByEmployeeAndDateRange(
                        1L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));

        assertEquals(1, result.size());
    }

    @Test
    void getLeaveByEmployeeId_success() {
        when(leaveRepository.findByEmployeeId(1L)).thenReturn(List.of(leave));
        when(modelMapper.map(leave, LeaveResponse.class)).thenReturn(response);

        assertEquals(1, leaveService.getLeaveByEmployeeId(1L).size());
    }

    @Test
    void getPendingLeaves_success() {
        when(leaveRepository.findByStatus(LeaveStatus.PENDING)).thenReturn(List.of(leave));
        when(modelMapper.map(leave, LeaveResponse.class)).thenReturn(response);

        assertEquals(1, leaveService.getPendingLeaves().size());
    }

    @Test
    void getAllLeaves_success() {
        when(leaveRepository.findAll()).thenReturn(List.of(leave));
        when(modelMapper.map(leave, LeaveResponse.class)).thenReturn(response);

        assertEquals(1, leaveService.getAllLeaves().size());
    }

    @Test
    void approveLeave_success() {
        when(leaveRepository.findById(10L)).thenReturn(Optional.of(leave));
        when(leaveRepository.save(leave)).thenReturn(leave);
        when(modelMapper.map(leave, LeaveResponse.class)).thenReturn(response);

        LeaveResponse result = leaveService.approveLeave(10L, "Manager");

        assertNotNull(result);
        assertEquals(LeaveStatus.APPROVED, leave.getStatus());
        assertEquals("Manager", leave.getApprovedBy());
        verify(leaveRepository).save(leave);
    }

    @Test
    void approveLeave_notFound() {
        when(leaveRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leaveService.approveLeave(999L, "Manager"));

        verify(leaveRepository, never()).save(any(Leave.class));
    }

    @Test
    void rejectLeave_success() {
        when(leaveRepository.findById(10L)).thenReturn(Optional.of(leave));
        when(leaveRepository.save(leave)).thenReturn(leave);
        when(modelMapper.map(leave, LeaveResponse.class)).thenReturn(response);

        LeaveResponse result = leaveService.rejectLeave(10L);

        assertNotNull(result);
        assertEquals(LeaveStatus.REJECTED, leave.getStatus());
        verify(leaveRepository).save(leave);
    }

    @Test
    void rejectLeave_notFound() {
        when(leaveRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leaveService.rejectLeave(999L));

        verify(leaveRepository, never()).save(any(Leave.class));
    }

    @Test
    void deleteLeave_success() {
        when(leaveRepository.findById(10L)).thenReturn(Optional.of(leave));

        leaveService.deleteLeave(10L);

        verify(leaveRepository).delete(leave);
    }

    @Test
    void deleteLeave_notFound() {
        when(leaveRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leaveService.deleteLeave(999L));

        verify(leaveRepository, never()).delete(any(Leave.class));
    }
}
