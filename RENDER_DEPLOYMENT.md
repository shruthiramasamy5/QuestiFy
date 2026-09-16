# 🚀 Deploying QuestiFy to Render (Single Public URL)

This guide walks you through deploying the complete **QuestiFy – AI Powered Question Paper Generation System** to **Render** using a **Render Blueprint (`render.yaml`)**.

Once deployed, your entire application (13 microservices, databases, API Gateway, and React frontend) will be accessible via **ONE SINGLE PUBLIC URL** (e.g., `https://questify-frontend.onrender.com`).

---

## 🏛️ Architecture: The Single Public URL Pattern

```
                                    +-----------------------------------------+
                                    |              Any Web Browser            |
                                    |   https://questify-frontend.onrender.com|
                                    +--------------------+--------------------+
                                                         | (HTTPS :443)
                                                         v
+-------------------------------------------------------------------------------------------------------------------------+
| Render Cloud Environment                                                                                                |
|                                                                                                                         |
|  +-------------------------------------------------------------+                                                        |
|  | questify-frontend (Render Web Service)                     |                                                        |
|  | - Serves React SPA for page routes (/)                      |                                                        |
|  | - Internal Reverse-Proxy for /api/* requests                |                                                        |
|  +------------------------------+------------------------------+                                                        |
|                                 | (Internal HTTP over Render Private Network)                                           |
|                                 v                                                                                       |
|  +-------------------------------------------------------------+                                                        |
|  | questify-api-gateway (:8000)                                |                                                        |
|  | Internal edge router to backend fleet                       |                                                        |
|  +-----+------------------------+------------------------+-----+                                                        |
|        |                        |                        |                                                              |
|        v                        v                        v                                                              |
|  +------------+          +------------+           +------------+                                                        |
|  | auth-      |          | institution|           | question-  |  ... (all 11 microservices)                            |
|  | service    |          | service    |           | bank-serv  |                                                        |
|  | (:8081)    |          | (:8082)    |           | (:8083)    |                                                        |
|  +-----+------+          +-----+------+           +-----+------+                                                        |
|        |                       |                        |                                                               |
|        |                       |                  +-----+------+                                                        |
|        |                       |                  | ai-service |                                                        |
|        |                       |                  | (:8084)    |                                                        |
|        |                       |                  +------------+                                                        |
|        +-----------------------+------------------------+                                                               |
|        |                                                |                                                               |
|        v                                                v                                                               |
|  +---------------------------+                    +---------------------------+                                         |
|  | questify-mysql (:3306)    |                    | questify-mongodb (:27017) |                                         |
|  | (or Cloud MySQL)          |                    | (or MongoDB Atlas)        |                                         |
|  +---------------------------+                    +---------------------------+                                         |
+-------------------------------------------------------------------------------------------------------------------------+
```

### Why this is the best deployment architecture:
1. **One Single Public URL**: Users only need `https://questify-frontend.onrender.com`.
2. **Zero CORS Headaches**: The frontend calls relative `/api/*` paths on its own domain. Nginx forwards them internally to the API Gateway.
3. **Enterprise Security**: All microservices, Eureka, and databases communicate over Render's internal private network. None of their internal ports are exposed to the public internet.

---

## 📋 Prerequisites

1. A free account on [GitHub](https://github.com).
2. A free account on [Render](https://render.com).
3. *(Optional)* A free Google Gemini API Key from [Google AI Studio](https://aistudio.google.com/) for AI generation.

---

## ⚡ Step-by-Step Deployment

### Step 1: Push Your Project to GitHub

1. Open PowerShell in your project folder (`C:\Users\HP\Desktop\QuestiFy`):
   ```powershell
   cd C:\Users\HP\Desktop\QuestiFy
   ```
2. Initialize Git, stage all files, and commit:
   ```powershell
   git init
   git add .
   git commit -m "feat: complete dockerized QuestiFy project with Render blueprint"
   ```
3. Create a **new private (or public) repository** on GitHub named `QuestiFy`.
4. Link your local repo and push:
   ```powershell
   git branch -M main
   git remote add origin https://github.com/<your-username>/QuestiFy.git
   git push -u origin main
   ```

---

### Step 2: Deploy on Render via Blueprint

1. Go to your [Render Dashboard](https://dashboard.render.com).
2. Click the blue **"New +"** button in the top right.
3. Select **"Blueprint"**.
4. Connect your GitHub account and choose the **`QuestiFy`** repository.
5. Render will detect the **`render.yaml`** file in your repo root.
6. Give your Blueprint instance a name (e.g. `questify-app`).
7. **Environment Variables Prompt**:
   - `GEMINI_API_KEY`: Paste your Google Gemini API Key (or leave blank if testing without live AI).
   - `QUESTIFY_JWT_SECRET` and `DB_PASSWORD`: Render will automatically generate secure, random 256-bit values for you (`generateValue: true`).
8. Click **"Apply"**!

Render will now automatically provision and build all services in your fleet!

---

### Step 3: Get Your Single Public URL

1. Once the `questify-frontend` service shows a green checkmark (**"Live"**):
2. Look at the top of the `questify-frontend` service page. You will see your public URL:
   👉 **`https://questify-frontend-xxxx.onrender.com`**
3. Click this URL. Your QuestiFy application is live on the internet!

---

## 🔑 Login Credentials

The `auth-service` database seeder automatically initializes demo accounts for all roles:

- **Password for ALL Accounts:** `Questify@123`
- **Institution Admin:** `admin@questify.dev` *(or `admin@questify.com`)*
- **Super Admin:** `superadmin@questify.dev` *(or `superadmin@questify.com`)*
- **Controller of Examinations (COE):** `coe@questify.dev` *(or `coe@questify.com`)*
- **Dean:** `dean@questify.dev` *(or `dean@questify.com`)*
- **Head of Department (HOD):** `hod@questify.dev` *(or `hod@questify.com`)*
- **Faculty:** `faculty@questify.dev` *(or `faculty@questify.com`)*

---

## 💾 Database Options: Free Cloud Databases vs. Self-Hosted Docker

### Option A: Fully Self-Hosted on Render (Default in `render.yaml`)
`render.yaml` automatically creates `questify-mysql` (with all schemas pre-initialized via `docker/mysql/Dockerfile`) and `questify-mongodb` inside your Render network. No external database signups needed.

### Option B: Free Cloud Databases (Recommended for Free Tier Persistence)
Because Render Free Tier containers reset when sleeping, you can optionally connect to 100% free cloud databases with persistent storage:

1. **MongoDB**: Create a free M0 cluster on [MongoDB Atlas](https://www.mongodb.com/atlas) (512MB free forever).
   - In Render Dashboard -> Environment Groups -> `questify-fleet-config`, add or edit:
     - `MONGODB_URI` = `mongodb+srv://<user>:<password>@cluster0.mongodb.net/paper_generation_service?retryWrites=true&w=majority`
2. **MySQL**: Create a free database on [TiDB Cloud](https://tidbcloud.com/) or [Clever Cloud](https://www.clever-cloud.com/).
   - In Render Dashboard -> Environment Groups -> `questify-fleet-config`, add or edit:
     - `SPRING_DATASOURCE_URL` = `jdbc:mysql://<host>:<port>/questify_auth?createDatabaseIfNotExist=true&useSSL=true`
     - `DB_USERNAME` = `<cloud-username>`
     - `DB_PASSWORD` = `<cloud-password>`

All microservices will immediately switch to the cloud database without code changes!

---

## 🛠️ Verification & Troubleshooting Checklist

| Issue | Cause | Solution |
| :--- | :--- | :--- |
| **First load is slow (takes ~30-50s)** | Free tier spin-up delay | On Render Free Tier, services sleep after 15 min of inactivity. The first request wakes them up. Subsequent requests are fast. |
| **"Unable to reach server" error** | API Gateway still starting | Wait 30 seconds for the Spring Boot Gateway and Eureka discovery heartbeat to synchronize. |
| **Custom Public Domain** | Want a custom URL (e.g. `app.questify.dev`) | Go to `questify-frontend` in Render -> **Settings** -> **Custom Domains** and add your CNAME record. |
| **Live AI questions failing** | Missing Gemini API Key | Set `GEMINI_API_KEY` in Render Dashboard -> `questify-fleet-config` environment group. |

---

## 🎯 Summary
You now have a production-grade cloud deployment architecture:
- **1 Public URL**
- **0 Port Conflicts**
- **0 Hardcoded Secrets**
- **100% Microservice Architecture Preserved**
