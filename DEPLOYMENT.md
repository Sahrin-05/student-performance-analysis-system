# Student Performance Analysis System (SPAS) — Production Deployment Runbook

This runbook provides complete, production-grade instructions for deploying and running the **Student Performance Analysis System (SPAS)** on public cloud hosting providers with HTTPS termination, automated database migrations, health probes, and strict zero-secret security.

---

## 1. System Architecture Overview

```
                          Internet (HTTPS / Port 443)
                                      │
                                      ▼
             ┌──────────────────────────────────────────────────┐
             │       Public Ingress & TLS Termination          │
             │   (Cloudflare / Nginx / Cloud Load Balancer)     │
             └────────────────────────┬─────────────────────────┘
                                      │
                   ┌──────────────────┴──────────────────┐
                   │                                     │
           /* (Static SPA & Assets)                 /api/* & /actuator/*
                   │                                     │
                   ▼                                     ▼
     ┌───────────────────────────┐         ┌───────────────────────────┐
     │    Nginx Web Server       │         │  Spring Boot REST API     │
     │  (React, TypeScript, Vite)│         │     (Java 21, Temurin)    │
     │  Container Port: 80       │         │  Container Port: 8080     │
     └───────────────────────────┘         └─────────────┬─────────────┘
                                                         │
                                                  JDBC / SSL Mode
                                                         │
                                                         ▼
                                           ┌───────────────────────────┐
                                           │       PostgreSQL 16       │
                                           │  (Managed Cloud / Volume) │
                                           │  Container Port: 5432     │
                                           └───────────────────────────┘
```

- **Frontend:** React 19 + TypeScript + Vite + Tailwind CSS v4, built to static HTML/JS/CSS and served via hardened Nginx Alpine with SPA routing and API reverse proxying.
- **Backend:** Java 21 (Eclipse Adoptium) + Spring Boot 4 + Spring Security (JWT) + Spring Data JPA + Flyway migrations + Spring Boot Actuator health checks.
- **Database:** PostgreSQL 16 with composite unique constraints, indexes, and full relational integrity across all 14 entities.

---

## 2. Zero-Secret Configuration & Environment Variables

All secrets, database credentials, and host-specific domains MUST be injected via runtime environment variables. Never commit secrets, passwords, or production JWT keys to source control.

### Required Production Environment Variables

| Variable | Description | Example / Format |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `prod` |
| `DATABASE_URL` or `DB_URL` | PostgreSQL connection URI or JDBC URL | `postgres://spas_user:secret@host.provider.com:5432/spas_db?sslmode=require` or `jdbc:postgresql://host.provider.com:5432/spas_db?sslmode=require` |
| `DB_USERNAME` | Database username (fallback if omitted in URI) | `spas_user` |
| `DB_PASSWORD` | Database password (fallback if omitted in URI) | `YourStrongDbPassword!` |
| `FLYWAY_ENABLED` | Enables Flyway schema migrations on startup | `true` |
| `JWT_SECRET` | 256-bit+ HMAC-SHA secret for token signing | Run `openssl rand -base64 48` to generate |
| `JWT_EXPIRATION_MS` | Access token lifespan in milliseconds | `86400000` (24 hours) |
| `CORS_ALLOWED_ORIGINS` | Comma-separated list of explicit allowed HTTPS origins (wildcard `*` is strictly forbidden when credentials are enabled) | `https://spas.university.edu,https://app.university.edu` |
| `ADMIN_EMAIL` | Optional seed email for initial root admin | `admin@spas.edu` |
| `ADMIN_PASSWORD` | Optional seed password for initial root admin | `AdminStrongPass2026!` |
| `ADMIN_IDENTIFIER` | Optional seed identifier for initial root admin | `ADMIN001` |
| `ADMIN_FULL_NAME` | Optional seed full name for root admin | `System Administrator` |

> [!NOTE]
> SPAS includes an automated `DatabaseConfig` normalizer that dynamically parses cloud-provided `postgres://` and `postgresql://` URIs (standard on Render, Railway, Fly.io, Heroku, Supabase, Neon) into standard `jdbc:postgresql://` URLs while extracting embedded credentials and preserving SSL parameters (`?sslmode=require`).

---

## 3. Database Migration Strategy (Flyway)

SPAS uses Flyway for reproducible, version-controlled database migrations with baseline protection.

### Clean Cloud Database (New Installation)
- When starting against an empty cloud PostgreSQL instance, Flyway detects an empty database and runs migrations in sequential order:
  1. `V1__initial_schema.sql`: Creates core academic and performance tables (`users`, `departments`, `courses`, `subjects`, `lecturer_assignments`, `enrollments`, `assessments`, `student_marks`, `subject_result_publications`, `attendance_sessions`, `attendance_records`, `risk_alerts`, `mark_audit_logs`).
  2. `V2__admin_audit_logs.sql`: Creates the system governance and administrative audit trail table (`admin_audit_logs`).
- All 14 core relational tables and composite unique constraints are initialized from scratch.
- If configured, `AdminInitializer` verifies whether a root administrator exists and seeds the account securely if absent.

### Existing Database Adoption Procedure (Pre-Flyway Databases)
For databases that were initialized prior to Flyway and already contain production data:
1. **Automated Baseline & Delta:**
   - Both `application.properties` and `application-prod.properties` are configured with:
     ```properties
     spring.flyway.baseline-on-migrate=true
     spring.flyway.baseline-version=1
     spring.flyway.baseline-description=Initial baseline for existing database
     ```
   - When Flyway connects to an unversioned non-empty database, it records baseline version `1` in `flyway_schema_history` and skips running `V1`. Existing user accounts, student marks, and attendance data are preserved untouched.
   - Flyway then detects pending migration `V2__admin_audit_logs.sql` (version 2 > baseline 1) and executes it automatically.
   - Because `V2__admin_audit_logs.sql` uses `CREATE TABLE IF NOT EXISTS` and `CREATE INDEX IF NOT EXISTS`, it is strictly idempotent:
     - If `admin_audit_logs` does not exist, it creates the table.
     - If `admin_audit_logs` already exists (e.g. from local Hibernate dev updates), it safely no-ops without error.

> [!WARNING]
> **Schema Mismatch Limitation:** `CREATE TABLE IF NOT EXISTS` checks only table name existence; it does **not** inspect or alter columns if `admin_audit_logs` already exists with incompatible legacy columns. In production with `ddl-auto=validate`, Hibernate will reject mismatched columns at boot. Verify column parity using Step B below.

2. **Pre-Adoption Verification Checklist:**
   Before running Flyway against a pre-existing production database:
   - **Step A (Backup):** Create a snapshot backup using `pg_dump -Fc spas_db > spas_db_pre_flyway.dump`.
   - **Step B (Verify Baseline Tables & Columns):** Check that the existing database contains the 13 core tables:
     ```sql
     SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name;
     ```
     If `admin_audit_logs` already exists, confirm its column structure matches the entity:
     ```sql
     SELECT column_name, data_type, is_nullable
     FROM information_schema.columns
     WHERE table_name = 'admin_audit_logs'
     ORDER BY ordinal_position;
     ```
     Required columns: `id`, `action`, `entity_type`, `entity_id`, `entity_identifier`, `details`, `performed_by_id`, `performed_by_email`, `performed_at`.
   - **Step C (Launch):** Start the application. Flyway will baseline at version 1, apply V2, and Hibernate will validate all 14 tables successfully under `ddl-auto=validate`.

3. **Migration Immutability & Future Schema Changes:**
   - **Immutability:** Once deployed, `V1__initial_schema.sql` and `V2__admin_audit_logs.sql` must **never** be edited retroactively. Any retroactive change will trigger Flyway checksum validation failures (`ValidateException`) on existing databases.
   - **Future Evolutions:** All future database changes (new tables, column additions, indexes) must be introduced strictly as new versioned migrations (e.g. `V3__add_xyz.sql`, `V4__...sql`).

---

## 4. Containerized Deployment (Docker Compose)

The project includes a root `docker-compose.yml` that orchestrates PostgreSQL 16, the Spring Boot API, and the Nginx frontend.

### Prerequisites
- Docker Engine 24+ and Docker Compose v2+ installed.

### Launching the Stack
1. Create a `.env` file in the project root:
   ```bash
   cp .env.example .env # or create .env with your production values
   ```
2. Start the services:
   ```bash
   docker compose up -d --build
   ```
3. Verify container health status:
   ```bash
   docker compose ps
   ```
   All three containers should report `(healthy)`.

4. Tail runtime logs:
   ```bash
   docker compose logs -f backend
   docker compose logs -f frontend
   ```

---

## 5. Cloud Platform Deployment Guide

### Option A: Render.com / Railway
1. **Database:** Deploy a Managed PostgreSQL 16 service. Copy the `Internal Database URL` (`postgres://...`).
2. **Backend Service:**
   - Create a Web Service pointing to the GitHub repository (Root Directory: `backend`).
   - Environment: Docker (uses `backend/Dockerfile`).
   - Add environment variables:
     - `SPRING_PROFILES_ACTIVE=prod`
     - `DATABASE_URL` = paste PostgreSQL URL
     - `JWT_SECRET` = generated 256-bit string
     - `CORS_ALLOWED_ORIGINS` = `https://<your-frontend-domain>.onrender.com`
     - `ADMIN_EMAIL` = `admin@spas.edu`
     - `ADMIN_PASSWORD` = strong password
   - Health Check Path: `/actuator/health`
3. **Frontend Service:**
   - Create a Web Service pointing to the GitHub repository (Root Directory: `frontend`).
   - Environment: Docker (uses `frontend/Dockerfile`).
   - Set environment variable `VITE_API_BASE_URL` = `https://<your-backend-domain>.onrender.com/api` (or rely on Nginx proxy).

### Option B: Cloud VPS / AWS EC2 with Docker & Let's Encrypt
1. Install Docker and Docker Compose on the Ubuntu 22.04 / 24.04 server.
2. Clone the repository and configure `.env`.
3. Set up Certbot / Nginx Proxy Manager / Caddy for automatic Let's Encrypt SSL on port 443 proxying to container port 80.
4. Run `docker compose up -d --build`.

---

## 6. Post-Deployment Verification & Smoke Tests

After deployment, perform these verification steps against the public HTTPS URL:

### 1. Actuator Health Probe
```bash
curl -i https://<backend-domain>/actuator/health
```
**Expected Response:**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{"status":"UP"}
```

### 2. Admin Login & Authorization
1. Navigate to `https://<frontend-domain>/` and click **Sign In**.
2. Log in using `admin@spas.edu` and the configured `ADMIN_PASSWORD`.
3. Verify access to:
   - User Management & Account Approvals Queue
   - Academic Structure Hub
   - Institutional Risk Alerts
   - Reports & Analytics Dossier
   - System Audit Trail Explorer

### 3. Registration & Approval Lifecycle
1. Open an incognito browser window, navigate to the public URL, and submit a Student Registration request (`STU-TEST-001`, `test.student@spas.edu`).
2. Verify response reports `PENDING_APPROVAL`.
3. Attempt to log in with the new credentials; verify that the login is blocked with **Account Pending Approval**.
4. In the Admin session, open **User Directory & Approvals**, locate the applicant in the pending queue, and click **Approve Account**.
5. Return to the incognito window and log in. Verify successful entry into the Student Dashboard.
6. In the Admin session, open **System Audit Trail** and verify the `USER_APPROVED` event is logged with the admin actor and timestamp.
