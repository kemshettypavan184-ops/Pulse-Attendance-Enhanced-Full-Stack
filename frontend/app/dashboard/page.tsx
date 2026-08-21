import Navbar from '@/components/Navbar';

const summary = [
    { label: 'Present', value: 42 },
    { label: 'Late', value: 6 },
    { label: 'Absent', value: 3 },
    { label: 'On Leave', value: 2 },
];

export default function DashboardPage() {
    return (
        <main>
            <Navbar />
            <div style={{ padding: 24 }}>
                <h1>Dashboard</h1>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: 16, marginTop: 16 }}>
                    {summary.map((item) => (
                        <div key={item.label} style={{ background: '#fff', padding: 20, borderRadius: 12, boxShadow: '0 4px 16px rgba(0,0,0,0.05)' }}>
                            <div style={{ color: '#6b7280', fontSize: 14 }}>{item.label}</div>
                            <div style={{ fontSize: 28, fontWeight: 700, marginTop: 8 }}>{item.value}</div>
                        </div>
                    ))}
                </div>
            </div>
        </main>
    );
}
