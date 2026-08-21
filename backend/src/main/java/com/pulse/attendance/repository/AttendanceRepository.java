package com.pulse.attendance.repository;

import com.pulse.attendance.entity.Attendance;
import com.pulse.attendance.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByEmployeeIdAndAttendanceDateBetween(
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate);

    List<Attendance> findByAttendanceDateAndStatus(
            LocalDate attendanceDate,
            AttendanceStatus status);
}