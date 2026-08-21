export type AttendanceRecord = {
    id: number;
    employeeId: string;
    employeeName: string;
    date: string;
    punchInTime: string | null;
    punchOutTime: string | null;
    status: string;
    active: boolean;
};
