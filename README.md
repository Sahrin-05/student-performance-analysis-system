# Student Performance Analysis System (SPAS)

A full-stack enterprise university academic performance analysis, continuous assessment, attendance tracking, and early-warning intervention platform.

[![Backend Tests](https://img.shields.io/badge/Backend%20Tests-277%20Passed-brightgreen.svg)](#testing)
[![Frontend Tests](https://img.shields.io/badge/Frontend%20Tests-22%20Passed-brightgreen.svg)](#testing)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F.svg?logo=springboot&logoColor=white)](#technology-stack)
[![React](https://img.shields.io/badge/React-19-61DAFB.svg?logo=react&logoColor=black)](#technology-stack)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.x-3178C6.svg?logo=typescript&logoColor=white)](#technology-stack)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-v4-38B2AC.svg?logo=tailwindcss&logoColor=white)](#technology-stack)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1.svg?logo=postgresql&logoColor=white)](#technology-stack)

---

## 1. System Overview

The **Student Performance Analysis System (SPAS)** is designed for universities and academic institutions to manage academic structures, administer continuous assessments, calculate grades and GPA/CGPA, track student attendance, and trigger early interventions for students at risk of academic failure or attendance shortages.

### Core Modules & Functional Capabilities

```
┌────────────────────────────────────────────────────────────────────────┐
│               Student Performance Analysis System (SPAS)               │
├───────────────────┬───────────────────┬────────────────────────────────┤
│ Identity & Access │ Academic Hub      │ Evaluation & Grades            │
│ • Role-Based RBAC │ • Departments     │ • Continuous Assessments (CA)  │
│ • Admin Approval  │ • Degree Programs │ • Strict Ruhuna Grades (A-F,W) │
│ • BCrypt & JJWT   │ • Course Modules  │ • DRAFT / SUBMITTED / PUBLISHED│
│ • Secure Tokens   │ • Enrollments     │ • GPA / CGPA Calculation       │
├───────────────────┼───────────────────┼────────────────────────────────┤
│ Attendance Engine │ Risk Intervention │ Institutional Analytics        │
│ • Session Logs    │ • Low Attendance  │ • Performance Analytics        │
│ • Aggregate Rate  │ • Score Shortfall │ • Class Grade Distributions    │
│ • <80% Warning    │ • Deduplication   │ • CSV Data Exports             │
│ • Marks Decoupled │ • Status Workflow │ • Durable Admin Audit Trail    │
└───────────────────┴───────────────────┴────────────────────────────────┘
```

1. **Role-Based Access Control (RBAC):**
   - **Administrator (`ADMIN`):** Oversees user registration approvals, role assignments, department and course setups, risk alert monitoring, system analytics, and governance audit trails.
   - **Lecturer / Teacher (`LECTURER`):** Enters marks for assigned modules, creates assessment breakdowns, tracks lecture attendance, submits grades for publication, and monitors module-level risk alerts.
   - **Student (`STUDENT`):** Views enrolled courses, published marks, semester GPA/cumulative CGPA transcripts, attendance percentage, and academic advisory notices.
   - **Academic Advisor / HOD (`ADVISOR` / `HOD`):** Reviews cross-departmental student performance, monitors cohort-level risk metrics, and guides interventions.

2. **Self-Registration & Approval Lifecycle:**
   - New users self-register as `STUDENT` or `LECTURER`.
   - Accounts are placed into `PENDING` state until explicitly reviewed and approved by an `ADMIN`.
   - Rejected or inactive accounts are barred from authenticating.
   - Users can track their approval status publicly using their university email and index number.

3. **Academic Structure & Enrollments:**
   - Hierarchical data model: `Department` → `Course` (Degree Program) → `Subject` (Module) → `Enrollment`.
   - Unique constraints prevent duplicate course codes, subject codes, or duplicate student enrollments in the same academic session.

4. **Continuous Assessment & Grading Policy:**
   - Multi-component evaluations: Assignments, Quizzes, Midterms, Practicals, and Final Examinations.
   - **Strict Grade Assignment:**
     - **A:** Percentage $\ge$ 75.0% (Grade Point: 4.0)
     - **B:** Percentage $\ge$ 65.0% (Grade Point: 3.0)
     - **C:** Percentage $\ge$ 50.0% (Grade Point: 2.0)
     - **S:** Percentage $\ge$ 40.0% (Grade Point: 1.0 — Satisfactory / Pass)
     - **F:** Percentage $<$ 40.0% (Grade Point: 0.0 — Fail)
     - **W:** Withdrawn (Assigned *exclusively* when an enrollment is dropped; never assigned based on a numeric mark).
   - **Three-Stage Result Workflow:**
     - `DRAFT`: Marks are editable by the lecturer.
     - `SUBMITTED`: Marks are locked for departmental moderation.
     - `PUBLISHED`: Marks are finalized, released to students, and factored into official GPA/CGPA transcripts.

5. **Attendance Tracking & Decoupled Architecture:**
   - Conducted session-by-session with dates, session titles, and individual attendance flags (`PRESENT`, `ABSENT`, `EXCUSED`).
   - Attendance percentage is calculated dynamically based on total logged sessions.
   - **Decoupled Design:** Attendance does *not* artificially manipulate numeric academic marks. Instead, attendance shortages trigger independent warnings and risk alerts.
   - An automated warning indicator highlights attendance below the institutional **80% threshold**.

6. **Early-Warning Risk Alert Engine:**
   - Detects academic distress:
     - **ATTENDANCE_RISK:** Attendance falls below 80%.
     - **ACADEMIC_RISK:** Continuous assessment or total marks fall below passing criteria ($< 40\%$).
     - **GPA_RISK:** Cumulative GPA falls below institutional probation threshold ($< 2.0$).
   - **Deduplication:** Prevents spamming instructors by guaranteeing that no duplicate open alert exists for the same student, subject, and reason.
   - **Resolution Workflow:** Alerts progress through `OPEN` → `UNDER_REVIEW` → `RESOLVED` / `DISMISSED`.

7. **Institutional Analytics & Admin Audit Trail:**
   - Interactive charts and reports for grade distributions, pass/fail ratios, and class averages.
   - One-click CSV export utility for official reporting.
   - Immutable administrative audit log (`admin_audit_logs`) tracking administrative actions (user approval, status transitions, role adjustments) with bounded database pagination.

---

## 2. Technology Stack

| Layer | Technologies |
|---|---|
| **Frontend** | React 19, TypeScript 5, Vite 8, Tailwind CSS v4, Lucide React, Vitest, Testing Library |
| **Backend** | Java 21 (Eclipse Adoptium), Spring Boot 4.1.1, Spring Security 6, Spring Data JPA, Hibernate ORM 7, JJWT 0.12.6, Flyway 11, Spring Boot Actuator |
| **Database** | PostgreSQL 16 (Production) / In-Memory H2 with PostgreSQL syntax mode (Integration Testing) |
| **Containerization** | Docker (multi-stage builds), Docker Compose, Nginx Alpine (Reverse Proxy & SPA Router) |
| **Build & Tooling** | Apache Maven (via Maven Wrapper `mvnw`), npm, oxlint |

---

## 3. Architecture & Repository Structure

```
student-performance-analysis-system/
├── backend/                              # Spring Boot REST API
│   ├── src/main/java/com/sahrin/spas/
│   │   ├── config/                       # Security, JWT, Database & Admin Initializer
│   │   ├── controller/                   # REST Controllers (Auth, Admin, Lecturer, Student, Reports)
│   │   ├── dto/                          # Request & Response Data Transfer Objects
│   │   ├── entity/                       # JPA Relational Entities (14 core tables)
│   │   ├── exception/                    # Global Exception Handler & Custom Errors
│   │   ├── repository/                   # Spring Data JPA Repositories
│   │   ├── service/                      # Business Logic & Grading Policy
│   │   └── util/                         # CSV Export and String Formatters
│   ├── src/main/resources/
│   │   ├── application.properties        # Local Development Configuration
│   │   ├── application-prod.properties   # Hardened Production Configuration (ddl-auto=validate)
│   │   └── db/migration/                 # Versioned Flyway SQL Migrations (V1, V2)
│   ├── src/test/                         # Comprehensive Backend Test Suite (277 tests)
│   └── pom.xml                           # Maven Dependencies & Build Setup
├── frontend/                             # React + TypeScript + Vite Single Page Application
│   ├── src/
│   │   ├── services/                     # Typed API client services with JWT bearer handling
│   │   ├── test/                         # Vitest + React Testing Library suites
│   │   ├── App.tsx                       # Root SPA Router & Navigation Shell
│   │   └── ...                           # Role-specific dashboard views
│   ├── Dockerfile                        # Multi-stage production Nginx container
│   ├── nginx.conf                        # Reverse proxy configuration for /api and SPA fallback
│   ├── package.json                      # Frontend scripts and dependencies
│   └── vite.config.ts                    # Vite configuration with local dev API proxy
├── .env.example                          # Environment variable template with placeholders
├── .gitignore                            # Secrets & local artifacts protection
├── docker-compose.yml                    # Unified orchestration (PostgreSQL, Backend, Frontend)
├── DEPLOYMENT.md                         # Production cloud deployment runbook
└── README.md                             # Project documentation
```

---

## 4. Local Development Setup

### Prerequisites
- **Java Development Kit (JDK):** Version 21 (e.g. Eclipse Adoptium Temurin 21)
- **Node.js & npm:** Node.js 20+ and npm 10+
- **PostgreSQL:** Version 15 or 16 installed locally, or run via Docker
- **Git**

---

### Step 1: Clone the Repository

```bash
git clone https://github.com/Sahrin-05/student-performance-analysis-system.git
cd student-performance-analysis-system
```

---

### Step 2: Database Setup

Ensure PostgreSQL is running and create the local development database:

```sql
CREATE DATABASE spas_db;
```

---

### Step 3: Backend Configuration & Startup

1. Copy `.env.example` to configure your environment, or export local variables:
   ```bash
   # Windows PowerShell
   $env:DB_URL = "jdbc:postgresql://localhost:5432/spas_db"
   $env:DB_USERNAME = "postgres"
   $env:DB_PASSWORD = "your_postgres_password"
   $env:JWT_SECRET = "YourSecureRandomSecretKeyForLocalDevMustBeAtLeast32BytesLong"
   $env:ADMIN_EMAIL = "admin@spas.edu"
   $env:ADMIN_PASSWORD = "AdminPassword123!"
   ```

2. Start the Spring Boot backend using the Maven Wrapper:
   ```bash
   cd backend
   .\mvnw.cmd spring-boot:run
   # On macOS/Linux: ./mvnw spring-boot:run
   ```
   The backend will start on **`http://localhost:8080`**.

   > [!NOTE]
   > On initial startup with `ADMIN_EMAIL` and `ADMIN_PASSWORD` defined, the system will automatically seed the root administrator account.

---

### Step 4: Frontend Configuration & Startup

1. Open a new terminal and navigate to the `frontend/` directory:
   ```bash
   cd frontend
   npm install
   ```

2. Start the Vite development server:
   ```bash
   npm run dev
   ```
   The application will be accessible at **`http://localhost:5173`**.

   > [!TIP]
   > Vite is configured with a development proxy (`/api` → `http://localhost:8080`). You do not need to configure custom CORS origins for local development.

---

## 5. Docker Compose (Full Stack)

To run the entire system (PostgreSQL database, Spring Boot backend, and Nginx frontend) with a single command:

1. Create a `.env` file from `.env.example` and supply your database password and JWT secret:
   ```bash
   cp .env.example .env
   ```

2. Launch all services:
   ```bash
   docker compose up --build
   ```

3. Access the services:
   - **Web Application:** `http://localhost` (Port 80 via Nginx reverse proxy)
   - **Backend API (Direct):** `http://localhost:8080/api`
   - **Actuator Health Probe:** `http://localhost:8080/actuator/health`

---

## 6. Database Migration & Safety Strategy (Flyway)

SPAS implements a dual-mode Flyway migration strategy designed for zero data loss and automated adoption across both greenfield and legacy environments:

### 1. Clean Database (Greenfield Install)
- `V1__initial_schema.sql`: Establishes the core relational schema covering all 13 primary tables (`users`, `departments`, `courses`, `subjects`, `lecturer_assignments`, `enrollments`, `assessments`, `student_marks`, `subject_result_publications`, `attendance_sessions`, `attendance_records`, `risk_alerts`, `mark_audit_logs`).
- `V2__admin_audit_logs.sql`: Creates table 14 (`admin_audit_logs`) for administrative governance.
- Clean installations apply `V1` then `V2` sequentially.

### 2. Existing Database Adoption (Zero-Data-Loss)
- Configured with `spring.flyway.baseline-on-migrate=true` and `spring.flyway.baseline-version=1`.
- When connected to an existing database containing tables 1–13, Flyway automatically records baseline version `1` without dropping or overwriting data.
- It then executes delta migration `V2__admin_audit_logs.sql` using idempotent `CREATE TABLE IF NOT EXISTS` syntax to ensure table 14 is present.
- Production uses `spring.jpa.hibernate.ddl-auto=validate` to guarantee strict schema conformance at boot time.

---

## 7. Testing & Quality Assurance

The codebase includes comprehensive automated tests covering unit logic, security rules, controllers, repositories, pagination limits, and schema migrations.

### Backend Automated Tests (277 Tests)
Run all backend tests using the Maven Wrapper against isolated in-memory H2 databases (zero connection to live databases):

```bash
cd backend
.\mvnw.cmd test
# On macOS/Linux: ./mvnw test
```

**Test Suite Breakdown:**
- `FlywayMigrationTest`: 5 test cases verifying clean install, existing baseline adoption, idempotency, and schema mismatch detection.
- `DatabaseConfigTest`: 5 test cases verifying cloud database URI parsing (`postgres://`, `postgresql://`, SSL modes, credentials).
- `SecurityConfigTest`: 4 test cases verifying stateless sessions, BCrypt hashing, and CORS wildcard rejection.
- `AdminControllerTest`, `AdminAuditControllerTest`, `AdminAcademicControllerTest`: 30+ test cases verifying RBAC, input sanitization, and bounded pagination.
- `AcademicServiceTest`, `PerformanceServiceTest`, `AttendanceServiceTest`, `RiskAlertServiceTest`: 150+ business logic test cases.
- `GradingPolicyTest`: 7 test cases validating the Ruhuna grading scale (A, B, C, S, F, W).
- `AdminInitializerTest`: 7 test cases validating root admin seeding and idempotency.

### Frontend Automated Tests (22 Tests)
Run frontend Vitest tests and linting:

```bash
cd frontend
# Run unit tests
npm test

# Run code linter
npm run lint

# Run production TypeScript build
npm run build
```

---

## 8. API Reference Summary

All API endpoints are prefixed with `/api`. Authentication is passed via the standard HTTP header:
`Authorization: Bearer <jwt-token>`

| Group | Method | Endpoint | Access Role | Description |
|---|---|---|---|---|
| **Auth** | `POST` | `/api/auth/register` | Public | Submit registration request (`STUDENT` / `LECTURER`) |
| **Auth** | `POST` | `/api/auth/login` | Public | Authenticate with identifier/email & password |
| **Auth** | `POST` | `/api/auth/status` | Public | Check registration approval status |
| **Admin** | `GET` | `/api/admin/users/pending` | `ADMIN` | List pending registrations (supports pagination) |
| **Admin** | `PUT` | `/api/admin/users/{id}/status` | `ADMIN` | Approve or reject a user (`ACTIVE` / `REJECTED`) |
| **Admin** | `GET` | `/api/admin/audit-logs` | `ADMIN` | Query administrative audit logs (bounded pagination) |
| **Academic** | `GET` | `/api/academic/departments` | Authenticated | List all academic departments |
| **Academic** | `POST`| `/api/academic/admin/courses` | `ADMIN` | Create a new degree course |
| **Academic** | `POST`| `/api/academic/admin/subjects` | `ADMIN` | Create a new subject module |
| **Academic** | `POST`| `/api/academic/admin/enroll` | `ADMIN` | Enroll student into a subject |
| **Marks** | `POST` | `/api/performance/lecturer/marks` | `LECTURER` | Save student marks (`DRAFT` / `SUBMITTED`) |
| **Marks** | `POST` | `/api/performance/lecturer/publish` | `LECTURER`, `ADMIN` | Publish moderated subject results |
| **Marks** | `GET`  | `/api/performance/student/results` | `STUDENT` | View published grades and GPA transcript |
| **Attendance** | `POST` | `/api/attendance/lecturer/sessions` | `LECTURER` | Record class session attendance |
| **Attendance** | `GET`  | `/api/attendance/student/my-attendance` | `STUDENT` | View session logs and attendance percentage |
| **Risk** | `GET`  | `/api/risk-alerts/admin` | `ADMIN` | Paginated risk alert registry across the institution |
| **Risk** | `PUT`  | `/api/risk-alerts/{id}/status` | `ADMIN`, `LECTURER` | Transition risk alert status (`UNDER_REVIEW`, `RESOLVED`) |
| **Actuator** | `GET` | `/actuator/health` | Public | System liveness and readiness probe |

---

## 9. Security & Production Deployment

For complete instructions on deploying SPAS to production environments (Render, Railway, Fly.io, AWS, VPS) with HTTPS termination, managed PostgreSQL, zero-secret injection, and automated backups, refer to:

👉 **[DEPLOYMENT.md](DEPLOYMENT.md)**

---

## 10. License

This project is developed for academic institutional management. All rights reserved.
