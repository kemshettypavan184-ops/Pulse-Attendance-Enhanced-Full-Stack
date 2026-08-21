const records = [
    { id: 1, employee: 'Jane Doe', date: '2026-08-13', checkIn: '08:30 AM', checkOut: '05:30 PM', status: 'Present' },
    { id: 2, employee: 'John Smith', date: '2026-08-13', checkIn: '09:10 AM', checkOut: '06:05 PM', status: 'Late' },
    { id: 3, employee: 'Alicia Brown', date: '2026-08-13', checkIn: '08:45 AM', checkOut: '05:15 PM', status: 'Present' },
];

export default function AttendanceTable() {
    return (
        <div style={{ background: '#fff', borderRadius: 12, overflow: 'hidden', boxShadow: '0 4px 16px rgba(0,0,0,0.05)' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead style={{ background: '#f3f4f6' }}>
                    <tr>
                        <th style={{ textAlign: 'left', padding: 12 }}>Employee</th>
                        <th style={{ textAlign: 'left', padding: 12 }}>Date</th>
                        <th style={{ textAlign: 'left', padding: 12 }}>Check In</th>
                        <th style={{ textAlign: 'left', padding: 12 }}>Check Out</th>
                        <th style={{ textAlign: 'left', padding: 12 }}>Status</th>
                    </tr>
                </thead>
                <tbody>
                    {records.map((record) => (
                        <tr key={record.id} style={{ borderTop: '1px solid #e5e7eb' }}>
                            <td style={{ padding: 12 }}>{record.employee}</td>
                            <td style={{ padding: 12 }}>{record.date}</td>
                            <td style={{ padding: 12 }}>{record.checkIn}</td>
                            <td style={{ padding: 12 }}>{record.checkOut}</td>
                            <td style={{ padding: 12 }}>
                                <span style={{ background: record.status === 'Present' ? '#dcfce7' : '#fef3c7', color: record.status === 'Present' ? '#166534' : '#92400e', padding: '4px 8px', borderRadius: 999 }}>{record.status}</span>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}
