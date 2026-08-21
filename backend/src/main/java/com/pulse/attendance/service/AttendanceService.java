package com.pulse.attendance.service;

import com.pulse.attendance.dto.AttendanceRequest;
import com.pulse.attendance.dto.AttendanceResponse;
import com.pulse.attendance.entity.Attendance;
import com.pulse.attendance.entity.Employee;
import com.pulse.attendance.enums.AttendanceStatus;
import com.pulse.attendance.exception.ResourceNotFoundException;
import com.pulse.attendance.repository.AttendanceRepository;
import com.pulse.attendance.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AttendanceService {

        private final AttendanceRepository attendanceRepository;
        private final EmployeeRepository employeeRepository;
        private final ModelMapper modelMapper;

        // =========================================================
        // CREATE / MARK ATTENDANCE
        // =========================================================
        @SuppressWarnings("null")
        public AttendanceResponse markAttendance(AttendanceRequest request) {

                log.info(
                                "Starting attendance marking for employeeId={}",
                                request.getEmployeeId());

                Employee employee = employeeRepository.findById(
                                request.getEmployeeId()).orElseThrow(() -> {

                                        log.warn(
                                                        "Attendance marking failed: employee not found with id={}",
                                                        request.getEmployeeId());

                                        return new ResourceNotFoundException(
                                                        "Employee not found with id: "
                                                                        + request.getEmployeeId());
                                });

                Attendance attendance = modelMapper.map(
                                request,
                                Attendance.class);

                attendance.setEmployee(employee);

                Attendance savedAttendance = attendanceRepository.save(attendance);

                log.info(
                                "Attendance marked successfully for employeeId={}, attendanceId={}",
                                request.getEmployeeId(),
                                savedAttendance.getId());

                return mapToResponse(savedAttendance);
        }

        // =========================================================
        // GET ATTENDANCE BY ID
        // =========================================================
        @SuppressWarnings("null")
        @Transactional(readOnly = true)
        public AttendanceResponse getAttendanceById(Long id) {

                log.info(
                                "Fetching attendance record with id={}",
                                id);

                Attendance attendance = attendanceRepository.findById(id)
                                .orElseThrow(() -> {

                                        log.warn(
                                                        "Attendance not found with id={}",
                                                        id);

                                        return new ResourceNotFoundException(
                                                        "Attendance not found with id: " + id);
                                });

                log.info(
                                "Attendance fetched successfully with id={}",
                                id);

                return mapToResponse(attendance);
        }

        // =========================================================
        // GET ATTENDANCE BY EMPLOYEE AND DATE RANGE
        // =========================================================
        @Transactional(readOnly = true)
        public List<AttendanceResponse> getAttendanceByEmployeeAndDateRange(
                        Long employeeId,
                        LocalDate startDate,
                        LocalDate endDate) {

                log.info(
                                "Fetching attendance for employeeId={} between {} and {}",
                                employeeId,
                                startDate,
                                endDate);

                List<AttendanceResponse> attendanceList = attendanceRepository
                                .findByEmployeeIdAndAttendanceDateBetween(
                                                employeeId,
                                                startDate,
                                                endDate)
                                .stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());

                log.info(
                                "Attendance search completed for employeeId={}: {} records found",
                                employeeId,
                                attendanceList.size());

                return attendanceList;
        }

        // =========================================================
        // GET PRESENT EMPLOYEES BY DATE
        // =========================================================
        @Transactional(readOnly = true)
        public List<AttendanceResponse> getAttendanceByDate(
                        LocalDate date) {

                log.info(
                                "Fetching present employees for date={}",
                                date);

                List<AttendanceResponse> attendanceList = attendanceRepository
                                .findByAttendanceDateAndStatus(
                                                date,
                                                AttendanceStatus.PRESENT)
                                .stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());

                log.info(
                                "Present employee search completed for date={}: {} records found",
                                date,
                                attendanceList.size());

                return attendanceList;
        }

        // =========================================================
        // GET ALL ATTENDANCE
        // =========================================================
        @Transactional(readOnly = true)
        public List<AttendanceResponse> getAllAttendance() {

                log.info("Fetching all attendance records");

                List<AttendanceResponse> attendanceList = attendanceRepository.findAll()
                                .stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());

                log.info(
                                "Fetched {} attendance records",
                                attendanceList.size());

                return attendanceList;
        }

        // =========================================================
        // UPDATE ATTENDANCE
        // =========================================================
        @SuppressWarnings("null")
        public AttendanceResponse updateAttendance(
                        Long id,
                        AttendanceRequest request) {

                log.info(
                                "Updating attendance with id={} for employeeId={}",
                                id,
                                request.getEmployeeId());

                Attendance attendance = attendanceRepository.findById(id)
                                .orElseThrow(() -> {

                                        log.warn(
                                                        "Attendance update failed: attendance not found with id={}",
                                                        id);

                                        return new ResourceNotFoundException(
                                                        "Attendance not found with id: " + id);
                                });

                Employee employee = employeeRepository.findById(
                                request.getEmployeeId()).orElseThrow(() -> {

                                        log.warn(
                                                        "Attendance update failed: employee not found with id={}",
                                                        request.getEmployeeId());

                                        return new ResourceNotFoundException(
                                                        "Employee not found with id: "
                                                                        + request.getEmployeeId());
                                });

                modelMapper.map(
                                request,
                                attendance);

                attendance.setEmployee(employee);

                Attendance updatedAttendance = attendanceRepository.save(attendance);

                log.info(
                                "Attendance updated successfully with id={} for employeeId={}",
                                updatedAttendance.getId(),
                                request.getEmployeeId());

                return mapToResponse(updatedAttendance);
        }

        // =========================================================
        // DELETE ATTENDANCE
        // =========================================================
        @SuppressWarnings("null")
        public void deleteAttendance(Long id) {

                log.info(
                                "Deleting attendance with id={}",
                                id);

                Attendance attendance = attendanceRepository.findById(id)
                                .orElseThrow(() -> {

                                        log.warn(
                                                        "Attendance deletion failed: attendance not found with id={}",
                                                        id);

                                        return new ResourceNotFoundException(
                                                        "Attendance not found with id: " + id);
                                });

                attendanceRepository.delete(attendance);

                log.info(
                                "Attendance deleted successfully with id={}",
                                id);
        }

        // =========================================================
        // MAP ENTITY TO RESPONSE
        // =========================================================
        private AttendanceResponse mapToResponse(
                        Attendance attendance) {

                AttendanceResponse response = modelMapper.map(
                                attendance,
                                AttendanceResponse.class);

                if (attendance.getEmployee() != null) {

                        response.setEmployeeName(
                                        attendance.getEmployee().getFullName());
                }

                return response;
        }
}