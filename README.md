# 🛡️ SentinelX

### SOC Incident Management & Response Platform

SentinelX is a Java-based cybersecurity incident management and response platform designed to centralize the reporting, investigation, tracking, and resolution of cybersecurity incidents.

The system provides different interfaces and permissions for **Users, Analysts, and Supermods**, allowing an incident to move through its complete lifecycle — from initial reporting and assignment to investigation, containment, remediation, resolution, and closure.

---

## 🎯 Problem Statement

Cybersecurity incidents need to be handled in a structured and traceable manner. When incident information is managed through separate communication channels or manual records, it can become difficult to track incidents, assign responsibility, maintain investigation records, monitor response times, and maintain accountability.

SentinelX addresses this by providing a centralized platform for managing cybersecurity incidents and the activities associated with them.

---

## 💡 What SentinelX Does

SentinelX provides a centralized workflow for:

```text
Incident Report
      ↓
Incident Review
      ↓
Assignment
      ↓
Investigation
      ↓
Evidence / IOC Collection
      ↓
Containment
      ↓
Remediation
      ↓
Resolution
      ↓
Closure
```

The platform also provides dashboards, analytics, audit logging, notifications, SLA monitoring, and simulated incidents for testing and demonstration.

---

## 👥 User Roles

### 👤 User

Users can:

- Log into the platform
- Report cybersecurity incidents
- View incidents they have reported
- Track incident status and progress
- View incident details
- Receive relevant notifications
- Monitor their incident activity through the dashboard

Users have restricted access and cannot access analyst or Supermod administrative functions.

---

### 🕵️ Analyst

Analysts are responsible for investigating and responding to incidents.

They can:

- View available incidents
- Claim or receive assigned incidents
- Start investigations
- Update incident status
- Add investigation notes
- Add and manage evidence
- Record Indicators of Compromise (IOCs)
- Perform containment actions
- Perform remediation actions
- Resolve incidents
- Escalate incidents when required
- Monitor SLA status
- View incident-related information and analytics
- Simulate cybersecurity incidents for testing and demonstration

The Analyst workspace provides a more detailed view of incidents and their investigation lifecycle.

---

### 👑 Supermod

The Supermod has system-level management and monitoring capabilities.

Supermods can:

- View all incidents
- Monitor overall incident activity
- Assign incidents to analysts
- Reassign incidents when required
- Manage analysts
- Manage users
- Monitor incident statuses
- Monitor SLA information
- Review audit logs
- View system analytics and charts
- Review investigation information
- Manage cybersecurity-related records
- Simulate incidents for testing and demonstration
- Monitor the overall incident-response workflow

The Supermod dashboard provides a high-level view of the complete system.

---

## 🚨 Incident Management

Each incident can move through different stages of the response lifecycle.

Typical workflow:

```text
OPEN
 ↓
INVESTIGATING
 ↓
CONTAINED
 ↓
REMEDIATING
 ↓
RESOLVED
 ↓
CLOSED
```

Depending on the situation, an incident can also be escalated or reassigned.

This allows the system to maintain a clear record of what is happening to an incident at each stage.

---

## 🧬 IOC Management

SentinelX supports the recording of **Indicators of Compromise (IOCs)**.

An IOC is information that may indicate that a system has been compromised.

Examples include:

- IP addresses
- Domains
- URLs
- File hashes
- Other suspicious indicators

IOCs can be associated with incidents and used as part of the investigation process.

---

## 📎 Evidence Management

Analysts can associate evidence with incidents during an investigation.

Evidence can be used to document information collected during the investigation and maintain a record of material relevant to the incident.

SentinelX provides dedicated storage locations for evidence and generated reports.

---

## 📝 Investigation Notes

Analysts can maintain investigation notes associated with incidents.

This provides a structured record of observations, findings, and investigation activities instead of relying on external notes or communication channels.

---

## ⏱️ SLA Monitoring

SentinelX supports **Service Level Agreement (SLA)** monitoring for incidents.

SLA information helps identify whether an incident is being handled within its expected response timeframe.

The system can distinguish between incidents that are:

- Within SLA
- Approaching their deadline
- Beyond their expected handling time

This helps analysts and Supermods prioritize incidents.

---

## 📋 Audit Logging

Important actions performed within the system can be recorded in the audit log.

Examples include:

- Incident creation
- Incident assignment
- Status changes
- Investigation actions
- Administrative actions
- Other important system operations

Audit logging provides traceability and accountability.

---

## 🔔 Notifications

SentinelX provides notifications for relevant system events.

Notifications help users and security personnel stay informed about changes related to incidents and other activities.

---

## 📊 Dashboards & Analytics

Different roles receive different levels of information through their dashboards.

### User Dashboard

Provides a simplified view of the user's own incident activity.

### Analyst Dashboard

Provides investigation-oriented information such as incident workload, statuses, severity information, and analytics.

### Supermod Dashboard

Provides a broader system-level view containing information such as:

- Incident statistics
- Severity distribution
- Incident trends
- Analyst activity
- SLA information
- Overall system activity

Charts and visualizations are used to make security information easier to understand.

---

## 🎲 Incident Simulation

SentinelX includes an incident simulation feature for Analysts and Supermods.

The simulation can generate random cybersecurity incidents for testing and demonstration.

This allows the incident-management workflow and dashboard analytics to be demonstrated without requiring an actual security incident.

---

## 🏗️ System Architecture

SentinelX follows a layered Java application architecture.

```text
┌─────────────────────────────────────┐
│             USER INTERFACE          │
│             Java Swing              │
│                                     │
│ User | Analyst | Supermod           │
└──────────────────┬──────────────────┘
                   │
                   ↓
┌─────────────────────────────────────┐
│          SERVICE / BUSINESS LOGIC   │
│                Java                 │
└──────────────────┬──────────────────┘
                   │
                   ↓
┌─────────────────────────────────────┐
│          DATA ACCESS LAYER           │
│                 DAO                  │
└──────────────────┬──────────────────┘
                   │
                   ↓
┌─────────────────────────────────────┐
│                JDBC                  │
└──────────────────┬──────────────────┘
                   │
                   ↓
┌─────────────────────────────────────┐
│               MySQL                  │
│              Database                │
└─────────────────────────────────────┘
```

### Main architectural components

**UI Layer**

Provides the graphical interfaces for Users, Analysts, and Supermods.

**Service Layer**

Contains application and business logic used to process operations and enforce application workflows.

**DAO Layer**

Handles database-related operations and separates database access from the rest of the application.

**Database Layer**

MySQL stores persistent application data.

**JDBC**

Java Database Connectivity is used to connect the Java application with MySQL.

---

## 🗄️ Database

SentinelX uses MySQL for persistent data storage.

The project includes a database schema in:

```text
schema.sql
```

The database contains separate tables for different areas of the application, including:

- User and role information
- Login/security information
- Incidents
- Incident history
- Investigation notes
- Evidence
- Threat intelligence
- Indicators
- Incident actions
- Notifications
- Audit logs
- Analyst requests
- System settings

The **Incident** data acts as a central part of the system and is associated with information generated during the investigation and response process.

---

## 💻 Technology Stack

| Component | Technology |
|---|---|
| Programming Language | Java |
| Frontend | Java Swing |
| Backend | Java |
| Database | MySQL |
| Database Connectivity | JDBC |
| Build/Execution | Batch scripts |
| Version Control | Git |
| Repository | GitHub |

---

## 📁 Project Structure

```text
SentinelX/
│
├── src/
│   └── com/cybersentinel/
│       ├── config/
│       ├── dao/
│       ├── database/
│       ├── engine/
│       ├── model/
│       ├── security/
│       ├── service/
│       ├── test/
│       └── ui/
│           ├── analyst/
│           ├── charts/
│           ├── common/
│           ├── supermod/
│           ├── theme/
│           └── user/
│
├── lib/
│   └── mysql-connector-j-9.4.0.jar
│
├── data/
│   ├── evidence/
│   └── reports/
│
├── schema.sql
├── build.bat
├── run.bat
├── test.bat
├── gen_sources.ps1
└── .gitignore
```

---

## ⚙️ Setup

### Prerequisites

Before running SentinelX, ensure the required Java environment and MySQL server are available.

### 1. Clone the repository

```bash
git clone <(https://github.com/J-CODEXX/SentinelX.git)>
cd SentinelX
```

### 2. Configure MySQL

Create the required database and execute:

```text
schema.sql
```

The application is configured to use the MySQL database:

```text
cyber_projects
```

### 3. Configure database credentials

Database credentials should be provided through environment variables rather than committed to the repository.

```text
CYBERSENTINEL_DB_URL
CYBERSENTINEL_DB_USER
CYBERSENTINEL_DB_PASSWORD
```

Example:

```text
CYBERSENTINEL_DB_URL=jdbc:mysql://localhost:3306/cyber_projects
CYBERSENTINEL_DB_USER=root
CYBERSENTINEL_DB_PASSWORD=your_password
```

**Do not commit real database passwords, API keys, or other credentials to GitHub.**

---

## ▶️ Running SentinelX

The project includes batch scripts for common operations.

### Build

```text
build.bat
```

Compiles the Java source files.

### Run

```text
run.bat
```

Launches the SentinelX application.

### Test

```text
test.bat
```

Builds and runs the project's verification tests.

---

## 🔐 Security Considerations

SentinelX includes application-level security features such as:

- Role-based access control
- Login handling
- Failed-login lockout mechanisms
- Audit logging
- Incident access restrictions
- Controlled administrative operations

The current application is primarily an **incident management and response platform**. It does not claim to replace endpoint detection, antivirus, EDR, SIEM, or other dedicated security-monitoring technologies.

---

## 🎯 Project Objective

The objective of SentinelX is to demonstrate how a centralized cybersecurity incident management platform can organize the incident-response lifecycle.

The system brings together:

```text
Reporting
   +
Assignment
   +
Investigation
   +
Evidence
   +
IOC Management
   +
Response Actions
   +
SLA Monitoring
   +
Audit Logging
   +
Analytics
```

into a single application.

---

## 🚀 Future Enhancements

Potential future development includes:

- Endpoint monitoring agents
- Automated security-event collection
- Integration with SIEM platforms
- Integration with EDR solutions
- External threat-intelligence APIs
- Centralized server/API architecture
- Multi-machine deployment
- Automated alert generation
- Advanced threat correlation
- Email/SMS security notifications

---

## 📌 Project Status

**Status:** Functional Prototype / Academic Project

SentinelX currently provides a working Java-based incident management environment with role-based workflows, database persistence, investigation capabilities, analytics, audit logging, and incident simulation.

---

## 👨‍💻 Contributors

This project was developed as a collaborative project by a team of four members.

Each member contributed to different areas of the application including interface development, backend logic, database integration, cybersecurity functionality, testing, and system integration.

---

## ⚠️ Disclaimer

SentinelX is an educational and demonstration project designed to model cybersecurity incident-management workflows.

It should not be considered a replacement for a production SOC, SIEM, EDR, incident-response platform, or other enterprise security infrastructure without additional security hardening, testing, deployment architecture, monitoring, and operational controls.
