package com.pulse.attendance;

import com.pulse.attendance.dto.AttendanceRequest;
import com.pulse.attendance.dto.AttendanceResponse;
import com.pulse.attendance.entity.Attendance;
import com.pulse.attendance.entity.Employee;
import com.pulse.attendance.enums.AttendanceStatus;
import com.pulse.attendance.enums.EmployeeStatus;
import com.pulse.attendance.exception.ResourceNotFoundException;
import com.pulse.attendance.repository.AttendanceRepository;
import com.pulse.attendance.repository.EmployeeRepository;
import com.pulse.attendance.service.AttendanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock private AttendanceRepository attendanceRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private ModelMapper modelMapper;

    @InjectMocks private AttendanceService attendanceService;

    private Employee employee;
    private Attendance attendance;
    private AttendanceRequest request;
    private AttendanceResponse response;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setEmployeeId("EMP001");
        employee.setFullName("John Doe");
        employee.setStatus(EmployeeStatus.ACTIVE);

        request = new AttendanceRequest(
                1L, LocalDate.of(2026, 8, 20),
                LocalTime.of(9, 0), LocalTime.of(17, 30),
                AttendanceStatus.PRESENT, "Regular day");

        attendance = new Attendance();
        attendance.setId(10L);
        attendance.setEmployee(employee);
        attendance.setAttendanceDate(request.getAttendanceDate());
        attendance.setCheckInTime(request.getCheckInTime());
        attendance.setCheckOutTime(request.getCheckOutTime());
        attendance.setStatus(request.getStatus());
        attendance.setRemarks(request.getRemarks());

        response = mock(AttendanceResponse.class);
        when(response.getEmployeeId()).thenReturn(1L);
        when(response.getEmployeeName()).thenReturn("John Doe");
    }

    @Test
    void markAttendance_success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(modelMapper.map(request, Attendance.class)).thenReturn(attendance);
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        when(modelMapper.map(attendance, AttendanceResponse.class)).thenReturn(response);

        AttendanceResponse result = attendanceService.markAttendance(request);

        assertNotNull(result);
        assertEquals(1L, result.getEmployeeId());
        assertEquals("John Doe", result.getEmployeeName());
        verify(employeeRepository).findById(1L);
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void markAttendance_employeeNotFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> attendanceService.markAttendance(request));

        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void getAttendanceById_success() {
        when(attendanceRepository.findById(10L)).thenReturn(Optional.of(attendance));
        when(modelMapper.map(attendance, AttendanceResponse.class)).thenReturn(response);

        AttendanceResponse result = attendanceService.getAttendanceById(10L);

        assertNotNull(result);
        assertEquals("John Doe", result.getEmployeeName());
        verify(attendanceRepository).findById(10L);
    }

    @Test
    void getAttendanceById_notFound() {
        when(attendanceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> attendanceService.getAttendanceById(999L));
    }

    @Test
    void getAttendanceByEmployeeAndDateRange_returnsRecords() {
        when(attendanceRepository.findByEmployeeIdAndAttendanceDateBetween(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(attendance));
        when(modelMapper.map(attendance, AttendanceResponse.class)).thenReturn(response);

        List<AttendanceResponse> result =
                attendanceService.getAttendanceByEmployeeAndDateRange(
                        1L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));

        assertEquals(1, result.size());
        verify(attendanceRepository).findByEmployeeIdAndAttendanceDateBetween(
                eq(1L), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void getAttendanceByDate_returnsPresentRecords() {
        when(attendanceRepository.findByAttendanceDateAndStatus(
                LocalDate.of(2026, 8, 20), AttendanceStatus.PRESENT))
                .thenReturn(List.of(attendance));
        when(modelMapper.map(attendance, AttendanceResponse.class)).thenReturn(response);

        List<AttendanceResponse> result =
                attendanceService.getAttendanceByDate(LocalDate.of(2026, 8, 20));

        assertEquals(1, result.size());
    }

    @Test
    void getAllAttendance_returnsRecords() {
        when(attendanceRepository.findAll()).thenReturn(List.of(attendance));
        when(modelMapper.map(attendance, AttendanceResponse.class)).thenReturn(response);

        List<AttendanceResponse> result = attendanceService.getAllAttendance();

        assertEquals(1, result.size());
    }

    @Test
    void updateAttendance_success() {
        when(attendanceRepository.findById(10L)).thenReturn(Optional.of(attendance));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        when(modelMapper.map(attendance, AttendanceResponse.class)).thenReturn(response);

        AttendanceResponse result = attendanceService.updateAttendance(10L, request);

        assertNotNull(result);
        verify(attendanceRepository).save(attendance);
    }

    @Test
    void updateAttendance_notFound() {
        when(attendanceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> attendanceService.updateAttendance(999L, request));

        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void deleteAttendance_success() {
        when(attendanceRepository.findById(10L)).thenReturn(Optional.of(attendance));

        attendanceService.deleteAttendance(10L);

        verify(attendanceRepository).delete(attendance);
    }

    @Test
    void deleteAttendance_notFound() {
        when(attendanceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> attendanceService.deleteAttendance(999L));

        verify(attendanceRepository, never()).delete(any(Attendance.class));
    }
}
