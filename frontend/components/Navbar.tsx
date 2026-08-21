import Link from 'next/link';

export default function Navbar() {
    return (
        <nav style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '16px 24px', background: '#111827', color: '#fff' }}>
            <div style={{ fontWeight: 700 }}>Pulse</div>
            <div style={{ display: 'flex', gap: 16 }}>
                <Link href="/dashboard">Dashboard</Link>
                <Link href="/attendance">Attendance</Link>
                <Link href="/login">Login</Link>
            </div>
        </nav>
    );
}
