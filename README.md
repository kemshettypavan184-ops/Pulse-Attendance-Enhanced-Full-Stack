# Pulse Attendance - Full Stack

This project combines:

- `backend/` - Spring Boot + JPA + PostgreSQL backend
- `frontend/` - Next.js + React + TypeScript frontend

## Run Backend

Requirements:
- Java 17
- Maven
- PostgreSQL

From the project root:

```powershell
cd backend
mvn spring-boot:run
```

Backend default URL:
http://localhost:8080

Swagger:
http://localhost:8080/swagger-ui/index.html

## Run Frontend

Requirements:
- Node.js
- npm

Open a second terminal:

```powershell
cd frontend
npm install
npm run dev
```

Frontend:
http://localhost:3000

The frontend services are configured to call the backend at:
http://localhost:8080

## Run Both

Use two terminals:

Terminal 1:
```powershell
cd backend
mvn spring-boot:run
```

Terminal 2:
```powershell
cd frontend
npm install
npm run dev
```

Note: PostgreSQL must be configured according to the backend application's database settings before the backend can access the database.
"# Pulse-Attendance-Enhanced-Full-Stack" 
