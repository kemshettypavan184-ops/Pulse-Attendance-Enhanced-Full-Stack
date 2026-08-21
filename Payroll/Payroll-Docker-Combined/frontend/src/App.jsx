import React, { useEffect, useMemo, useState } from 'react'
import {
  Activity, ArrowUpRight, Banknote, Building2, Calculator, CheckCircle2,
  ChevronLeft, ChevronRight, CircleDollarSign, Edit3, LayoutDashboard,
  Menu, MoreHorizontal, Plus, Search, Settings, Trash2, TrendingUp,
  UserPlus, Users, X, Wallet, AlertCircle, RefreshCw
} from 'lucide-react'
import { api } from './api'

const emptyForm = {
  name: '', email: '', department: '', basicSalary: '', allowances: '', deductions: ''
}

const money = value => new Intl.NumberFormat('en-IN', {
  style: 'currency', currency: 'INR', maximumFractionDigits: 0
}).format(Number(value || 0))

function App() {
  const [employees, setEmployees] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [page, setPage] = useState('dashboard')
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [query, setQuery] = useState('')
  const [department, setDepartment] = useState('All departments')
  const [modal, setModal] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [payroll, setPayroll] = useState(null)
  const [toast, setToast] = useState('')
  const [searchPage, setSearchPage] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [sortBy, setSortBy] = useState('id')
  const [direction, setDirection] = useState('asc')
  const [minSalary, setMinSalary] = useState('')
  const [maxSalary, setMaxSalary] = useState('')
  const [pageMeta, setPageMeta] = useState({ totalElements: 0, totalPages: 0, number: 0, size: 10 })

  const loadEmployees = async () => {
    setLoading(true); setError('')
    try {
      const result = await api.getEmployees(0, 100)
      setEmployees(result?.content || [])
      setPageMeta(result || {})
    } catch (e) { setError(e.message) }
    finally { setLoading(false) }
  }

  const loadSearchResults = async (targetPage = searchPage) => {
    setLoading(true); setError('')
    try {
      const cleanQuery = query.trim()
      const salaryFromSearch = parseSalarySearch(cleanQuery)
      const result = await api.searchEmployees({
        name: salaryFromSearch === null && cleanQuery && !cleanQuery.includes('@') ? cleanQuery : '',
        email: salaryFromSearch === null && cleanQuery.includes('@') ? cleanQuery : '',
        department: department === 'All departments' ? '' : department,
        minBasicSalary: salaryFromSearch === null ? minSalary : '',
        maxBasicSalary: salaryFromSearch === null ? maxSalary : '',
        salary: salaryFromSearch,
        page: targetPage,
        size: pageSize,
        sortBy,
        direction
      })
      setEmployees(result?.content || [])
      setPageMeta(result || {})
    } catch (e) { setError(e.message) }
    finally { setLoading(false) }
  }

  useEffect(() => { loadEmployees() }, [])

  useEffect(() => {
    if (page !== 'employees') return
    const timer = setTimeout(() => loadSearchResults(searchPage), 250)
    return () => clearTimeout(timer)
  }, [page, query, department, minSalary, maxSalary, searchPage, pageSize, sortBy, direction])

  useEffect(() => {
    if (!toast) return
    const t = setTimeout(() => setToast(''), 2800)
    return () => clearTimeout(t)
  }, [toast])

  const departments = useMemo(
    () => ['All departments', ...new Set(employees.map(e => e.department).filter(Boolean))],
    [employees]
  )

  const stats = useMemo(() => {
    const payrollBase = employees.reduce((s, e) => s + Number(e.basicSalary || 0), 0)
    const allowances = employees.reduce((s, e) => s + Number(e.allowances || 0), 0)
    const deductions = employees.reduce((s, e) => s + Number(e.deductions || 0), 0)
    return {
      employees: employees.length,
      payroll: payrollBase + allowances - deductions,
      gross: payrollBase + allowances,
      deductions
    }
  }, [employees])

  const openCreate = () => {
    setForm(emptyForm); setModal({ type: 'employee', title: 'Add employee' })
  }

  const openEdit = employee => {
    setForm({
      name: employee.name || '',
      email: employee.email || '',
      department: employee.department || '',
      basicSalary: employee.basicSalary ?? '',
      allowances: employee.allowances ?? '',
      deductions: employee.deductions ?? ''
    })
    setModal({ type: 'employee', title: 'Edit employee', id: employee.id })
  }

  const submitEmployee = async e => {
    e.preventDefault()
    const payload = {
      ...form,
      basicSalary: Number(form.basicSalary),
      allowances: Number(form.allowances),
      deductions: Number(form.deductions)
    }
    try {
      if (modal.id) await api.updateEmployee(modal.id, payload)
      else await api.createEmployee(payload)
      setModal(null); setToast(modal.id ? 'Employee updated successfully' : 'Employee added successfully')
      await loadEmployees()
    } catch (e) { setToast(e.message) }
  }

  const remove = async id => {
    if (!confirm('Delete this employee?')) return
    try {
      await api.deleteEmployee(id)
      setToast('Employee deleted')
      await loadEmployees()
    } catch (e) { setToast(e.message) }
  }

  const showPayroll = async employee => {
    try {
      setPayroll(await api.payroll(employee.id))
      setModal({ type: 'payroll', title: 'Payroll summary' })
    } catch (e) { setToast(e.message) }
  }

  const nav = [
    { id: 'dashboard', label: 'Overview', icon: LayoutDashboard },
    { id: 'employees', label: 'Employees', icon: Users },
    { id: 'payroll', label: 'Payroll', icon: Wallet }
  ]

  return (
    <div className="app-shell">
      <aside className={`sidebar ${sidebarOpen ? 'open' : ''}`}>
        <div className="brand">
          <div className="brand-mark"><CircleDollarSign size={22}/></div>
          <div><strong>PayFlow</strong><span>Payroll platform</span></div>
        </div>
        <div className="nav-label">WORKSPACE</div>
        <nav>
          {nav.map(({id,label,icon:Icon}) => (
            <button key={id} className={page === id ? 'nav-item active' : 'nav-item'}
              onClick={() => { setPage(id); setSidebarOpen(false) }}>
              <Icon size={18}/><span>{label}</span>
            </button>
          ))}
        </nav>
        <div className="sidebar-bottom">
          <button className="nav-item"><Settings size={18}/><span>Settings</span></button>
          <div className="system-card"><span className="status-dot"></span><div><b>System online</b><small>API connected</small></div></div>
        </div>
      </aside>

      <main className="main">
        <header className="topbar">
          <button className="mobile-menu" onClick={() => setSidebarOpen(v => !v)}><Menu/></button>
          <div>
            <p className="eyebrow">PAYROLL MANAGEMENT</p>
            <h1>{page === 'dashboard' ? 'ENFEC-ONE' : page === 'employees' ? 'Employees' : 'Payroll'}</h1>
          </div>
          <div className="top-actions">
            <button className="icon-button" onClick={loadEmployees} title="Refresh"><RefreshCw size={18}/></button>
            <button className="profile"><span>SR</span><div><b>Srilok</b><small>Administrator</small></div></button>
          </div>
        </header>

        {error && <div className="alert error"><AlertCircle size={18}/><span>{error}</span><button onClick={loadEmployees}>Retry</button></div>}

        {page === 'dashboard' && (
          <Dashboard employees={employees} stats={stats} onEmployees={() => setPage('employees')}
            onPayroll={showPayroll} onAdd={openCreate} />
        )}

        {page === 'employees' && (
          <Employees employees={employees} departments={departments} query={query} setQuery={value => { setQuery(value); setSearchPage(0) }}
            department={department} setDepartment={value => { setDepartment(value); setSearchPage(0) }}
            minSalary={minSalary} setMinSalary={value => { setMinSalary(value); setSearchPage(0) }}
            maxSalary={maxSalary} setMaxSalary={value => { setMaxSalary(value); setSearchPage(0) }}
            sortBy={sortBy} setSortBy={value => { setSortBy(value); setSearchPage(0) }}
            direction={direction} setDirection={value => { setDirection(value); setSearchPage(0) }}
            pageSize={pageSize} setPageSize={value => { setPageSize(Number(value)); setSearchPage(0) }}
            currentPage={pageMeta.number ?? searchPage} totalPages={pageMeta.totalPages ?? 0} totalElements={pageMeta.totalElements ?? 0}
            setSearchPage={setSearchPage} loading={loading}
            onAdd={openCreate} onEdit={openEdit} onDelete={remove} onPayroll={showPayroll} />
        )}

        {page === 'payroll' && (
          <Payroll employees={employees} onPayroll={showPayroll} onAdd={openCreate} />
        )}
      </main>

      {modal?.type === 'employee' && (
        <Modal title={modal.title} onClose={() => setModal(null)}>
          <form onSubmit={submitEmployee} className="form-grid">
            <Field label="Full name" required><input value={form.name} onChange={e=>setForm({...form,name:e.target.value})} placeholder="e.g. Priya Sharma" required /></Field>
            <Field label="Email" required><input type="email" value={form.email} onChange={e=>setForm({...form,email:e.target.value})} placeholder="name@company.com" required /></Field>
            <Field label="Department" required><input value={form.department} onChange={e=>setForm({...form,department:e.target.value})} placeholder="Engineering" required /></Field>
            <Field label="Basic salary" required><input type="number" min="0" value={form.basicSalary} onChange={e=>setForm({...form,basicSalary:e.target.value})} placeholder="0" required /></Field>
            <Field label="Allowances"><input type="number" min="0" value={form.allowances} onChange={e=>setForm({...form,allowances:e.target.value})} placeholder="0" /></Field>
            <Field label="Deductions"><input type="number" min="0" value={form.deductions} onChange={e=>setForm({...form,deductions:e.target.value})} placeholder="0" /></Field>
            <div className="modal-actions"><button type="button" className="btn secondary" onClick={()=>setModal(null)}>Cancel</button><button className="btn primary">{modal.id ? 'Save changes' : 'Add employee'}</button></div>
          </form>
        </Modal>
      )}

      {modal?.type === 'payroll' && payroll && (
        <Modal title={modal.title} onClose={() => setModal(null)}>
          <div className="payroll-head"><div className="avatar large">{initials(payroll.employeeName)}</div><div><h2>{payroll.employeeName}</h2><p>Employee #{payroll.employeeId}</p></div></div>
          <div className="payroll-total"><span>Net salary</span><strong>{money(payroll.netSalary)}</strong></div>
          <div className="breakdown">
            <Row label="Basic salary" value={payroll.basicSalary}/>
            <Row label="Allowances" value={payroll.allowances} positive/>
            <Row label="Gross salary" value={payroll.grossSalary} strong/>
            <Row label="Deductions" value={payroll.deductions} negative/>
            <Row label="Net salary" value={payroll.netSalary} strong/>
          </div>
        </Modal>
      )}

      {toast && <div className="toast"><CheckCircle2 size={18}/>{toast}</div>}
    </div>
  )
}

function Dashboard({employees, stats, onEmployees, onPayroll, onAdd}) {
  const recent = employees.slice(-5).reverse()
  const departments = [...new Set(employees.map(e=>e.department).filter(Boolean))]
  return <section className="content">
    <div className="hero">
      <div><span className="pill">● LIVE PAYROLL</span><h2>One place to manage<br/><em>your entire payroll.</em></h2><p>Track employees, calculate salaries and keep payroll operations simple.</p></div>
      <button className="btn light" onClick={onAdd}><UserPlus size={17}/> Add employee</button>
    </div>
    <div className="stats-grid">
      <Stat icon={Users} label="Total employees" value={stats.employees} note="Active records"/>
      <Stat icon={Banknote} label="Net payroll" value={money(stats.payroll)} note="Current monthly total" />
      <Stat icon={TrendingUp} label="Gross payroll" value={money(stats.gross)} note="Before deductions"/>
      <Stat icon={Calculator} label="Deductions" value={money(stats.deductions)} note="Current total"/>
    </div>
    <div className="two-col">
      <div className="panel">
        <div className="panel-head"><div><h3>Recent employees</h3><p>Latest records in your payroll</p></div><button className="text-btn" onClick={onEmployees}>View all <ArrowUpRight size={15}/></button></div>
        <EmployeeList employees={recent} onPayroll={onPayroll}/>
      </div>
      <div className="panel">
        <div className="panel-head"><div><h3>Departments</h3><p>Workforce distribution</p></div></div>
        <div className="department-list">{departments.length ? departments.map(d => {
          const count = employees.filter(e=>e.department===d).length
          return <div className="dept-row" key={d}><div className="dept-icon"><Building2 size={17}/></div><div className="dept-info"><b>{d}</b><span>{count} employee{count!==1?'s':''}</span></div><strong>{Math.round(count/Math.max(employees.length,1)*100)}%</strong></div>
        }) : <Empty text="No department data yet."/>}</div>
      </div>
    </div>
  </section>
}

function Employees({employees,departments,query,setQuery,department,setDepartment,minSalary,setMinSalary,maxSalary,setMaxSalary,sortBy,setSortBy,direction,setDirection,pageSize,setPageSize,currentPage,totalPages,totalElements,setSearchPage,loading,onAdd,onEdit,onDelete,onPayroll}) {
  return <section className="content">
    <div className="page-title"><div><h2>Employee directory</h2><p>Advanced search, filtering, sorting and pagination.</p></div><button className="btn primary" onClick={onAdd}><Plus size={17}/> Add employee</button></div>
    <div className="panel">
      <div className="toolbar advanced-toolbar">
        <div className="search"><Search size={17}/><input value={query} onChange={e=>setQuery(e.target.value)} placeholder="Search name, email or salary (e.g. 50k)..." /></div>
        <select value={department} onChange={e=>setDepartment(e.target.value)}>{departments.map(d=><option key={d}>{d}</option>)}</select>
        <input className="filter-input" type="number" min="0" value={minSalary} onChange={e=>setMinSalary(e.target.value)} placeholder="Min salary" />
        <input className="filter-input" type="number" min="0" value={maxSalary} onChange={e=>setMaxSalary(e.target.value)} placeholder="Max salary" />
        <select value={sortBy} onChange={e=>setSortBy(e.target.value)}>
          <option value="id">Sort: ID</option><option value="name">Sort: Name</option><option value="email">Sort: Email</option>
          <option value="department">Sort: Department</option><option value="basicSalary">Sort: Basic salary</option>
          <option value="allowances">Sort: Allowances</option><option value="deductions">Sort: Deductions</option>
        </select>
        <select value={direction} onChange={e=>setDirection(e.target.value)}><option value="asc">Ascending</option><option value="desc">Descending</option></select>
      </div>
      <div className="table-wrap">
        {loading ? <div className="loading">Loading employees…</div> :
        employees.length ? <table><thead><tr><th>Employee</th><th>Department</th><th>Basic salary</th><th>Allowances</th><th>Net estimate</th><th></th></tr></thead>
        <tbody>{employees.map(e=><tr key={e.id}><td><div className="person"><div className="avatar">{initials(e.name)}</div><div><b>{e.name}</b><small>{e.email}</small></div></div></td><td><span className="badge">{e.department}</span></td><td>{money(e.basicSalary)}</td><td>{money(e.allowances)}</td><td><b>{money(Number(e.basicSalary)+Number(e.allowances)-Number(e.deductions))}</b></td><td><div className="actions"><button title="Calculate payroll" onClick={()=>onPayroll(e)}><Calculator size={16}/></button><button title="Edit" onClick={()=>onEdit(e)}><Edit3 size={16}/></button><button title="Delete" className="danger" onClick={()=>onDelete(e.id)}><Trash2 size={16}/></button></div></td></tr>)}</tbody></table>
        : <Empty text="No employees match your filters."/>}
      </div>
      <div className="pagination">
        <span>{totalElements} employee{totalElements === 1 ? '' : 's'} found</span>
        <div className="pagination-actions">
          <select value={pageSize} onChange={e=>setPageSize(e.target.value)} title="Rows per page"><option value="5">5 / page</option><option value="10">10 / page</option><option value="25">25 / page</option><option value="50">50 / page</option><option value="100">100 / page</option></select>
          <button disabled={currentPage <= 0 || loading} onClick={()=>setSearchPage(currentPage - 1)}><ChevronLeft size={16}/></button>
          <span>Page {(currentPage ?? 0) + 1} of {Math.max(totalPages || 1, 1)}</span>
          <button disabled={currentPage + 1 >= totalPages || loading} onClick={()=>setSearchPage(currentPage + 1)}><ChevronRight size={16}/></button>
        </div>
      </div>
    </div>
  </section>
}

function Payroll({employees,onPayroll,onAdd}) {
  const total = employees.reduce((s,e)=>s + Number(e.basicSalary||0)+Number(e.allowances||0)-Number(e.deductions||0),0)
  return <section className="content">
    <div className="page-title"><div><h2>Payroll center</h2><p>Calculate and review employee take-home pay.</p></div><button className="btn primary" onClick={onAdd}><Plus size={17}/> Add employee</button></div>
    <div className="payroll-banner"><div><div className="mini-icon"><Wallet size={19}/></div><div><span>Estimated net payroll</span><strong>{money(total)}</strong></div></div><span className="payroll-count">{employees.length} employees</span></div>
    <div className="payroll-grid">{employees.map(e=>{const net=Number(e.basicSalary||0)+Number(e.allowances||0)-Number(e.deductions||0);return <div className="salary-card" key={e.id}><div className="salary-top"><div className="person"><div className="avatar">{initials(e.name)}</div><div><b>{e.name}</b><small>{e.department}</small></div></div><button className="more" onClick={()=>onPayroll(e)}><MoreHorizontal size={19}/></button></div><div className="salary-main"><span>Net salary</span><strong>{money(net)}</strong></div><div className="salary-meta"><span>Gross {money(Number(e.basicSalary||0)+Number(e.allowances||0))}</span><span>Deducted {money(e.deductions)}</span></div><button className="salary-btn" onClick={()=>onPayroll(e)}>View breakdown <ChevronRight size={15}/></button></div>})}</div>
  </section>
}

function EmployeeList({employees,onPayroll}) {
  return employees.length ? <div className="compact-list">{employees.map(e=><div className="compact-row" key={e.id}><div className="person"><div className="avatar">{initials(e.name)}</div><div><b>{e.name}</b><small>{e.department}</small></div></div><div className="compact-right"><b>{money(Number(e.basicSalary)+Number(e.allowances)-Number(e.deductions))}</b><button onClick={()=>onPayroll(e)}><ChevronRight size={16}/></button></div></div>)}</div> : <Empty text="No employees yet."/>
}
function Stat({icon:Icon,label,value,note}) { return <div className="stat"><div className="stat-icon"><Icon size={19}/></div><div><span>{label}</span><strong>{value}</strong><small>{note}</small></div></div> }
function Field({label,children}) { return <label className="field"><span>{label}</span>{children}</label> }
function Row({label,value,positive,negative,strong}) { return <div className={`break-row ${strong?'strong':''}`}><span>{label}</span><b className={positive?'positive':negative?'negative':''}>{money(value)}</b></div> }
function Modal({title,onClose,children}) { return <div className="overlay" onMouseDown={e=>e.target===e.currentTarget&&onClose()}><div className="modal"><div className="modal-head"><h3>{title}</h3><button onClick={onClose}><X size={19}/></button></div>{children}</div></div> }
function Empty({text}) { return <div className="empty"><Users size={24}/><p>{text}</p></div> }
function initials(name='') { return name.split(' ').filter(Boolean).slice(0,2).map(x=>x[0]).join('').toUpperCase() || 'NA' }

function parseSalarySearch(value) {
  if (!value) return null

  // Supports: 50000, 50k, 50K, ₹50000, ₹50k, 50,000 and 50k salary.
  const normalized = value
    .toLowerCase()
    .replace(/₹/g, '')
    .replace(/,/g, '')
    .trim()

  const match = normalized.match(/^(\d+(?:\.\d+)?)\s*(k|thousand)?(?:\s*(?:salary|salaried))?$/)
  if (!match) return null

  const amount = Number(match[1])
  if (!Number.isFinite(amount) || amount < 0) return null
  return match[2] === 'k' || match[2] === 'thousand' ? amount * 1000 : amount
}

export default App
