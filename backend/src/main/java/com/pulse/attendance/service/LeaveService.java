package com.pulse.attendance.service;

import com.pulse.attendance.dto.LeaveRequest;
import com.pulse.attendance.dto.LeaveResponse;
import com.pulse.attendance.entity.Leave;
import com.pulse.attendance.entity.Employee;
import com.pulse.attendance.enums.LeaveStatus;
import com.pulse.attendance.exception.ResourceNotFoundException;
import com.pulse.attendance.repository.LeaveRepository;
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
public class LeaveService {

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;

    // =========================================================
    // APPLY LEAVE
    // =========================================================
    @SuppressWarnings("null")
    public LeaveResponse applyLeave(LeaveRequest request) {

        log.info(
                "Starting leave application for employeeId={}",
                request.getEmployeeId());

        Employee employee = employeeRepository.findById(
                request.getEmployeeId()).orElseThrow(() -> {

                    log.warn(
                            "Leave application failed: employee not found with id={}",
                            request.getEmployeeId());

                    return new ResourceNotFoundException(
                            "Employee not found with id: "
                                    + request.getEmployeeId());
                });

        Leave leave = modelMapper.map(
                request,
                Leave.class);

        leave.setEmployee(employee);

        Leave savedLeave = leaveRepository.save(leave);

        log.info(
                "Leave application created successfully with leaveId={} for employeeId={}",
                savedLeave.getId(),
                request.getEmployeeId());

        return mapToResponse(savedLeave);
    }

    // =========================================================
    // GET LEAVE BY ID
    // =========================================================
    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public LeaveResponse getLeaveById(Long id) {

        log.info(
                "Fetching leave with id={}",
                id);

        Leave leave = leaveRepository.findById(id)
                .orElseThrow(() -> {

                    log.warn(
                            "Leave not found with id={}",
                            id);

                    return new ResourceNotFoundException(
                            "Leave not found with id: " + id);
                });

        log.info(
                "Leave fetched successfully with id={}",
                id);

        return mapToResponse(leave);
    }

    // =========================================================
    // GET LEAVE BY EMPLOYEE AND DATE RANGE
    // =========================================================
    @Transactional(readOnly = true)
    public List<LeaveResponse> getLeaveByEmployeeAndDateRange(
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate) {

        log.info(
                "Fetching leave records for employeeId={} between {} and {}",
                employeeId,
                startDate,
                endDate);

        List<LeaveResponse> leaves = leaveRepository
                .findByEmployeeIdAndStartDateBetween(
                        employeeId,
                        startDate,
                        endDate)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        log.info(
                "Leave date-range search completed for employeeId={}: {} records found",
                employeeId,
                leaves.size());

        return leaves;
    }

    // =========================================================
    // GET LEAVE BY EMPLOYEE ID
    // =========================================================
    @Transactional(readOnly = true)
    public List<LeaveResponse> getLeaveByEmployeeId(
            Long employeeId) {

        log.info(
                "Fetching leave records for employeeId={}",
                employeeId);

        List<LeaveResponse> leaves = leaveRepository.findByEmployeeId(employeeId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        log.info(
                "Fetched {} leave records for employeeId={}",
                leaves.size(),
                employeeId);

        return leaves;
    }

    // =========================================================
    // GET PENDING LEAVES
    // =========================================================
    @Transactional(readOnly = true)
    public List<LeaveResponse> getPendingLeaves() {

        log.info("Fetching pending leave requests");

        List<LeaveResponse> leaves = leaveRepository.findByStatus(LeaveStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        log.info(
                "Fetched {} pending leave requests",
                leaves.size());

        return leaves;
    }

    // =========================================================
    // GET ALL LEAVES
    // =========================================================
    @Transactional(readOnly = true)
    public List<LeaveResponse> getAllLeaves() {

        log.info("Fetching all leave records");

        List<LeaveResponse> leaves = leaveRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        log.info(
                "Fetched {} leave records",
                leaves.size());

        return leaves;
    }

    // =========================================================
    // APPROVE LEAVE
    // =========================================================
    @SuppressWarnings("null")
    public LeaveResponse approveLeave(
            Long id,
            String approvedBy) {

        log.info(
                "Approving leave with id={}",
                id);

        Leave leave = leaveRepository.findById(id)
                .orElseThrow(() -> {

                    log.warn(
                            "Leave approval failed: leave not found with id={}",
                            id);

                    return new ResourceNotFoundException(
                            "Leave not found with id: " + id);
                });

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setApprovedBy(approvedBy);

        Leave updatedLeave = leaveRepository.save(leave);

        log.info(
                "Leave approved successfully with id={}",
                updatedLeave.getId());

        return mapToResponse(updatedLeave);
    }

    // =========================================================
    // REJECT LEAVE
    // =========================================================
    @SuppressWarnings("null")
    public LeaveResponse rejectLeave(Long id) {

        log.info(
                "Rejecting leave with id={}",
                id);

        Leave leave = leaveRepository.findById(id)
                .orElseThrow(() -> {

                    log.warn(
                            "Leave rejection failed: leave not found with id={}",
                            id);

                    return new ResourceNotFoundException(
                            "Leave not found with id: " + id);
                });

        leave.setStatus(LeaveStatus.REJECTED);

        Leave updatedLeave = leaveRepository.save(leave);

        log.info(
                "Leave rejected successfully with id={}",
                updatedLeave.getId());

        return mapToResponse(updatedLeave);
    }

    // =========================================================
    // DELETE LEAVE
    // =========================================================
    @SuppressWarnings("null")
    public void deleteLeave(Long id) {

        log.info(
                "Deleting leave with id={}",
                id);

        Leave leave = leaveRepository.findById(id)
                .orElseThrow(() -> {

                    log.warn(
                            "Leave deletion failed: leave not found with id={}",
                            id);

                    return new ResourceNotFoundException(
                            "Leave not found with id: " + id);
                });

        leaveRepository.delete(leave);

        log.info(
                "Leave deleted successfully with id={}",
                id);
    }

    // =========================================================
    // MAP ENTITY TO RESPONSE
    // =========================================================
    private LeaveResponse mapToResponse(Leave leave) {

        LeaveResponse response = modelMapper.map(
                leave,
                LeaveResponse.class);

        if (leave.getEmployee() != null) {
            response.setEmployeeName(
                    leave.getEmployee().getFullName());
        }

        return response;
    }
}