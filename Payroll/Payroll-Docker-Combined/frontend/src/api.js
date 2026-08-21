const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080'

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
    ...options
  })

  let body = null
  try { body = await response.json() } catch {}

  if (!response.ok) {
    let message = `Request failed (${response.status})`
    if (body) {
      message = body.message || body.error || message
      if (body.errors) message = Object.values(body.errors).join(', ')
    }
    throw new Error(message)
  }

  if (response.status === 204) return null
  // Enhanced backend wraps successful responses in ApiResponse { data: ... }.
  return body?.data !== undefined ? body.data : body
}

const queryString = params => {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') search.set(key, value)
  })
  return search.toString()
}

export const api = {
  searchEmployees: (params = {}) => {
    const query = queryString({
      name: params.name,
      email: params.email,
      department: params.department,
      minBasicSalary: params.minBasicSalary,
      maxBasicSalary: params.maxBasicSalary,
      salary: params.salary,
      page: params.page ?? 0,
      size: params.size ?? 10,
      sortBy: params.sortBy ?? 'id',
      direction: params.direction ?? 'asc'
    })
    return request(`/employees?${query}`)
  },

  // Convenience method for dashboard/payroll screens.
  getEmployees: (page = 0, size = 100) =>
    api.searchEmployees({ page, size, sortBy: 'id', direction: 'asc' }),

  getEmployee: id => request(`/employees/${id}`),
  createEmployee: data => request('/employees', { method: 'POST', body: JSON.stringify(data) }),
  updateEmployee: (id, data) => request(`/employees/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteEmployee: id => request(`/employees/${id}`, { method: 'DELETE' }),
  payroll: id => request(`/payroll/${id}`)
}
