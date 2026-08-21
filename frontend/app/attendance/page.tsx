import AttendanceTable from '@/components/AttendanceTable';
import Navbar from '@/components/Navbar';

export default function AttendancePage() {
    return (
        <main>
            <Navbar />
            <div style={{ padding: 24 }}>
                <h1>Attendance</h1>
                <AttendanceTable />
            </div>
        </main>
    );
}
