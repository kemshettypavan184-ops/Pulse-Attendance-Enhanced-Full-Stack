export async function fetchAttendance() {
    const response = await fetch('http://localhost:8080/api/attendance');

    if (!response.ok) {
        throw new Error('Failed to load attendance');
    }

    return response.json();
}
