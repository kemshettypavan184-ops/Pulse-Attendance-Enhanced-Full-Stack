'use client';

import { useState } from 'react';

export default function LoginPage() {
    const [username, setUsername] = useState('admin');
    const [password, setPassword] = useState('admin123');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        console.log('Login request', { username, password });
        alert('Demo login submitted. Connect to backend API to authenticate.');
    };

    return (
        <main style={{ minHeight: '100vh', display: 'grid', placeItems: 'center', background: '#eef4ff' }}>
            <form onSubmit={handleSubmit} style={{ width: 360, background: '#fff', padding: 24, borderRadius: 12, boxShadow: '0 8px 30px rgba(0,0,0,0.08)' }}>
                <h2 style={{ marginTop: 0 }}>Login</h2>
                <div style={{ marginBottom: 16 }}>
                    <label style={{ display: 'block', marginBottom: 8 }}>Username</label>
                    <input value={username} onChange={(e) => setUsername(e.target.value)} style={{ width: '100%', padding: 10, borderRadius: 8, border: '1px solid #ccc' }} />
                </div>
                <div style={{ marginBottom: 16 }}>
                    <label style={{ display: 'block', marginBottom: 8 }}>Password</label>
                    <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} style={{ width: '100%', padding: 10, borderRadius: 8, border: '1px solid #ccc' }} />
                </div>
                <button type="submit" style={{ width: '100%', padding: 12, background: '#2563eb', color: '#fff', border: 'none', borderRadius: 8, cursor: 'pointer' }}>
                    Sign In
                </button>
            </form>
        </main>
    );
}
