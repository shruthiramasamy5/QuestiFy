# 🐳 QuestiFy Docker Deployment Guide

Welcome to the Dockerized deployment of **QuestiFy – AI Powered Question Paper Generation System**.

This document covers everything you need to know to build, run, test, and manage the entire fleet of microservices, databases, and the React frontend using **Docker Compose**.

---

## 🏛️ System Architecture

The setup consists of **16 interconnected containers** communicating over an isolated bridge network (`questify-network`):

```
                                  +--------------------------------------------------------+
                                  |                     Browser Client                     |
                                  |                 http://localhost:5173                  |
                                  +---------------------------+----------------------------+
                                                              |
                                                              v
+-------------------------------------------------------------------------------------------------------------------------+
| Docker Network: questify-network                                                                                        |
|                                                                                                                         |
|  +-------------------------------------+                +------------------------------------+                          |
|  | questify-frontend (Nginx :80)       |                | questify-api-gateway (:8000)       |                          |
|  | http://localhost:5173               | ----------->   | http://localhost:8000              |                          |
|  +-------------------------------------+                +-----------------+------------------+                          |
|                                                                           |                                             |
|        +-------------------+-------------------+-------------------+------+-------------+--------------------+          |
|        |                   |                   |                   |                    |                    |          |
|        v                   v                   v                   v                    v                    v          |
|  +------------+     +------------+      +------------+      +------------+       +------------+       +------------+    |
|  | auth-      |     | institution|      | question-  |      | paper-gen- |       | papers-    |       | approval-  |    |
|  | service    |     | service    |      | bank-serv  |      | service    |       | service    |       | service    |    |
|  | (:8081)    |     | (:8082)    |      | (:8083)    |      | (:8085)    |       | (:8086)    |       | (:8087)    |    |
|  +-----+------+     +-----+------+      +-----+------+      +-----+------+       +-----+------+       +-----+------+    |
|        |                  |                   |                   |                    |                    |           |
|        |                  |                   |                   | +------------+     |                    |           |
|        |                  |                   |                   +>| ai-service |     |                    |           |
|        |                  |                   |                     | (:8084)    |     |                    |           |
|        |                  |                   |                     +------------+     |                    |           |
|        |                  |                   |                                        |                    |           |
|  +-----+------+     +-----+------+      +-----+------+      +------------+       +-----+------+                     |   |
|  | analytics- |     | backup-    |      | audit-log- |      | ml-health- |       | eureka-    |                     |   |
|  | service    |     | service    |      | service    |      | service    |       | server     |                     |   |
|  | (:8088)    |     | (:8089)    |      | (:8090)    |      | (:8091)    |       | (:8761)    |                     |   |
|  +-----+------+     +-----+------+      +-----+------+      +------------+       +------------+                     |   |
|        |                  |                   |                                                                     |   |
|        +------------------+-------------------+---------------------------------------------------------------------+   |
|        |                                      |                                                                         |
|        v                                      v                                                                         |
|  +---------------------------+          +---------------------------+                                                   |
|  | questify-mysql (:3306)    |          | questify-mongodb (:27017) |                                                   |
|  | Host Port: 3307           |          | Host Port: 27018          |                                                   |
|  +---------------------------+          +---------------------------+                                                   |
+-------------------------------------------------------------------------------------------------------------------------+
```

---

## 📋 Prerequisites

1. **Docker Desktop** installed on Windows.
   - Download: [https://www.docker.com/products/docker-desktop/](https://www.docker.com/products/docker-desktop/)
   - Ensure Docker Desktop is **running** (system tray whale icon is steady).
2. **Git** and **PowerShell** (available by default on Windows).

---

## ⚡ Quick Start

### 1. Configure Environment (Optional)
A preconfigured `.env` file is already included. You can inspect or modify `.env` to suit your requirements:
```bash
# Optional: supply your Gemini API key if using live AI generation
GEMINI_API_KEY=your_gemini_api_key_here
```

### 2. Start Docker Desktop
Make sure Docker Desktop is launched on your Windows host:
```powershell
Start-Process "C:\Users\HP\AppData\Local\Programs\DockerDesktop\Docker Desktop.exe"
```

### 3. Build and Launch the Entire Fleet
Run the following command from the project root directory (`C:\Users\HP\Desktop\QuestiFy`):
```bash
docker compose up -d --build
```

Docker Compose will automatically:
1. Initialize the `questify-network` bridge network.
2. Initialize persistent volumes: `mysql_data` and `mongodb_data`.
3. Start `questify-mysql` and execute `docker/mysql/init.sql` to prepare all database schemas.
4. Start `questify-mongodb`.
5. Build and launch `questify-eureka-server` and wait for health verification.
6. Build and launch all 11 backend microservices once dependencies are healthy.
7. Build and launch `questify-api-gateway`.
8. Build the React + Vite static bundle into an Nginx production container (`questify-frontend`).

---

## 🌐 Ports & Service Catalog

| Service | Container Port | Mapped Host Port | URL / Endpoint |
| :--- | :--- | :--- | :--- |
| **React Frontend** | 80 | **5173** | [http://localhost:5173](http://localhost:5173) |
| **Spring Cloud API Gateway** | 8000 | **8000** | [http://localhost:8000](http://localhost:8000) |
| **Eureka Service Registry** | 8761 | **8761** | [http://localhost:8761](http://localhost:8761) |
| **Auth Service** | 8081 | 8081 | [http://localhost:8081](http://localhost:8081) / `/api/auth` |
| **Institution Service** | 8082 | 8082 | [http://localhost:8082](http://localhost:8082) / `/api/institutions` |
| **Question Bank Service** | 8083 | 8083 | [http://localhost:8083](http://localhost:8083) / `/api/questions` |
| **AI Service** | 8084 | 8084 | [http://localhost:8084](http://localhost:8084) / `/api/ai` |
| **Paper Generation Service** | 8085 | 8085 | [http://localhost:8085](http://localhost:8085) / `/api/generate` |
| **Papers Service** | 8086 | 8086 | [http://localhost:8086](http://localhost:8086) / `/api/papers` |
| **Approval Service** | 8087 | 8087 | [http://localhost:8087](http://localhost:8087) / `/api/approvals` |
| **Analytics Service** | 8088 | 8088 | [http://localhost:8088](http://localhost:8088) / `/api/analytics` |
| **Backup Service** | 8089 | 8089 | [http://localhost:8089](http://localhost:8089) / `/api/backups` |
| **Audit Log Service** | 8090 | 8090 | [http://localhost:8090](http://localhost:8090) / `/api/audit-events` |
| **ML Health Service** | 8091 | 8091 | [http://localhost:8091](http://localhost:8091) / `/api/ml-health` |
| **MySQL Database** | 3306 | **3307** | `localhost:3307` (avoids host 3306 conflict) |
| **MongoDB Database** | 27017 | **27018** | `localhost:27018` (avoids host 27017 conflict) |

> **Note on Host Ports**:
> If your host has local MySQL (port 3306), MongoDB (port 27017), or Oracle TNSLSNR (port 8080) running, the Docker Compose configuration intentionally maps container ports to non-conflicting host ports (MySQL `3307`, MongoDB `27018`, Gateway `8000`, Frontend `5173`). All containers inside the Docker network talk to each other directly on default ports (`mysql:3306`, `mongodb:27017`).

---

## 🔑 Default Login Credentials

On startup, `auth-service` automatically seeds accounts for all six system roles.

**Password for ALL accounts:**
`Questify@123`

| Role | Email (`.dev`) | Email (`.com`) | Description |
| :--- | :--- | :--- | :--- |
| **Super Admin** | `superadmin@questify.dev` | `superadmin@questify.com` | Platform administration, cross-tenant management |
| **Institution Admin**| `admin@questify.dev` | `admin@questify.com` | Institution setup, user management, departments |
| **COE (Controller of Exams)**| `coe@questify.dev` | `coe@questify.com` | Exam creation, final approvals, release of papers |
| **Dean** | `dean@questify.dev` | `dean@questify.com` | Academic oversight, syllabus mapping, review |
| **HOD (Head of Department)**| `hod@questify.dev` | `hod@questify.com` | Departmental approvals, faculty assignment |
| **Faculty** | `faculty@questify.dev` | `faculty@questify.com` | Question bank entry, draft generation, evaluation |

---

## 🛠️ Common Operations & Management

### View Container Status
```bash
docker compose ps
```

### View Live Logs
- All services:
  ```bash
  docker compose logs -f
  ```
- Specific service (e.g. Gateway or Auth):
  ```bash
  docker compose logs -f api-gateway
  docker compose logs -f auth-service
  ```

### Stop the Fleet
```bash
docker compose stop
```

### Restart the Fleet
```bash
docker compose start
```

### Rebuild a Specific Service
If you update code in a specific service (e.g. `institution-service` or frontend):
```bash
docker compose up -d --build institution-service
docker compose up -d --build questify-frontend
```

### Tear Down (Preserving Database Data)
```bash
docker compose down
```

### Tear Down and Reset All Database Volumes
> ⚠️ **Warning**: This completely erases MySQL and MongoDB volumes and rebuilds fresh from seeds.
```bash
docker compose down -v
```

---

## 🔍 Verification & Health Inspection

1. **Service Discovery Status:**
   Visit [http://localhost:8761](http://localhost:8761) in your browser. You should see all 11 microservices registered with `UP` status.
2. **API Gateway Connectivity:**
   Test through gateway:
   ```bash
   curl http://localhost:8000/api/auth/users
   ```
3. **Frontend Application:**
   Open [http://localhost:5173](http://localhost:5173) in your browser. Login using `admin@questify.dev` and `Questify@123`.
