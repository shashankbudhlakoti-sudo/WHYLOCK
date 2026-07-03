# 🛡 WHYLOCK

<p align="center">
  <img src="https://img.shields.io/badge/Spring_Boot-3.x-success?style=for-the-badge&logo=springboot"/>
  <img src="https://img.shields.io/badge/React-19-blue?style=for-the-badge&logo=react"/>
  <img src="https://img.shields.io/badge/PostgreSQL-Database-blue?style=for-the-badge&logo=postgresql"/>
  <img src="https://img.shields.io/badge/Redis-Cache-red?style=for-the-badge&logo=redis"/>
  <img src="https://img.shields.io/badge/Groq-AI-purple?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Docker-Container-blue?style=for-the-badge&logo=docker"/>
</p>

<p align="center">
<b>Know Why A System Locks Before It Locks.</b>
</p>

---

# 🌐 Live Demo

### Frontend
https://whylock-frontend.onrender.com/

### Backend API
https://whylock-backend.onrender.com/

---

# 🚀 Overview

WHYLOCK is an AI-powered Website Security Intelligence Platform that analyzes websites, identifies vulnerabilities, calculates security risk scores, and generates professional security reports.

Unlike traditional scanners, WHYLOCK explains **why** a website is risky instead of simply displaying vulnerabilities.

---

# ✨ Features

## 🔍 AI Website Security Scan

- AI Powered Risk Analysis
- Website Security Inspection
- Risk Score (0-100)
- Security Summary
- Threat Detection
- Explainable AI Results

---

## 📄 Professional PDF Reports

Generate professional PDF security reports containing

- Risk Score
- Security Summary
- Findings
- Recommendations
- Scan Timestamp
- AI Model Used

---

## 📧 Email Reports

Automatically send

- Welcome Email
- Security Report
- Monitoring Alerts

using Brevo Email API.

---

## 📊 Monitoring System

Monitor websites continuously.

Receive alerts whenever

- Risk Score increases
- New Critical Vulnerability appears

---

## 🔐 Authentication

- JWT Authentication
- Secure Login
- Secure Registration
- Protected APIs

---

## 📜 Scan History

Every scan is stored permanently.

Users can

- View previous scans
- Download reports
- Compare security over time

---

# 🏗 Architecture

```
                React Frontend
                       │
                       ▼
           Spring Boot REST API
                       │
        ┌──────────────┼──────────────┐
        ▼              ▼              ▼
 PostgreSQL        Redis Cache      Groq AI
        │                             │
        └──────────────┬──────────────┘
                       ▼
               Security Analysis
                       │
                       ▼
               PDF + Email Reports
```

---

# 🛠 Tech Stack

## Frontend

- React
- Next.js
- TypeScript
- Tailwind CSS

## Backend

- Spring Boot
- Spring Security
- JWT
- REST APIs

## Database

- PostgreSQL

## Cache

- Redis

## AI

- Groq Llama 3.3

## Reports

- PDF Generator

## Email

- Brevo API

## Deployment

- Docker
- Render

---

# 📂 Project Structure

```
WHYLOCK

Frontend
│
├── React
├── Next.js
├── Tailwind
└── TypeScript

Backend
│
├── Spring Boot
├── Controllers
├── Services
├── Security
├── JWT
├── PostgreSQL
├── Redis
├── AI Engine
├── PDF Reports
└── Email Service
```

---

# 🔐 Security Workflow

```
User

    │

    ▼

Enter Website URL

    │

    ▼

Backend Validation

    │

    ▼

AI Risk Analysis

    │

    ▼

Risk Score Calculation

    │

    ▼

Security Findings

    │

    ▼

PDF Report

    │

    ▼

Email Report

    │

    ▼

Stored in History
```

---

# 📌 API Features

- User Registration
- User Login
- Website Scan
- Scan History
- PDF Download
- Email Report
- Website Monitoring

---

# 🐳 Docker

```bash
docker build -t whylock .
docker run -p 8085:8085 whylock
```

---

# ⚙ Environment Variables

```
SPRING_DATASOURCE_URL=
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=

JWT_SECRET=

GROQ_API_KEY=

BREVO_API_KEY=

SPRING_DATA_REDIS_HOST=
SPRING_DATA_REDIS_PORT=
```

---

# 🎯 Future Roadmap

- Malware Detection
- SSL Certificate Analysis
- WHOIS Lookup
- CVE Database Integration
- Live Threat Intelligence
- Browser Extension
- Enterprise Dashboard
- API Keys
- Multi User Organizations
- SIEM Integration

---

# 👨‍💻 Developer

**Shashank Budhlakoti**

B.Tech Computer Science Engineering

Cybersecurity | Java | Spring Boot | AI

---

# ⭐ Support

If you like this project, please give it a ⭐ on GitHub.

---

<p align="center">

WHYLOCK

Know Why A System Locks Before It Locks.

</p>
