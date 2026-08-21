import Link from 'next/link';

export default function HomePage() {
    return (
        <main style={{ padding: '40px', maxWidth: 960, margin: '0 auto' }}>
            <h1>Pulse Attendance</h1>
            <p>Welcome to the employee attendance management system.</p>
            <div style={{ display: 'flex', gap: '16px', marginTop: '24px' }}>
                <Link href="/login" style={{ padding: '10px 16px', background: '#2563eb', color: '#fff', borderRadius: 8 }}>
                    Login
                </Link>
                <Link href="/dashboard" style={{ padding: '10px 16px', background: '#111827', color: '#fff', borderRadius: 8 }}>
                    Dashboard
                </Link>
            </div>
        </main>
    );
}
