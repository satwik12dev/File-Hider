<div align="center">

# ⚙️ CypherVault - Backend Core Service
### *Spring Boot 3.3.0 • Java 21 LTS • MySQL BLOB Encapsulation • Anti-Forensic Wipe Engine*

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0%2B-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Hibernate](https://img.shields.io/badge/Hibernate-ORM-59666C?style=for-the-badge&logo=hibernate&logoColor=white)](https://hibernate.org/)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-Stateless-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)

<br/>

<p align="center">
  The <b>CypherVault Backend</b> is a mission-critical Java/Spring Boot microservice powering file isolation, zero-knowledge verification, in-memory OTP caches, and anti-forensic local disk sanitization.
</p>

</div>

---

## 📑 Table of Contents

- [Overview & Architecture](#-overview--architecture)
- [Key Features](#-key-features)
- [System Requirements](#-system-requirements)
- [Technology Stack](#-technology-stack)
- [Directory Structure](#-directory-structure)
- [Database Configuration](#-database-configuration)
- [Application Properties](#-application-properties)
- [Running the Backend](#-running-the-backend)
  - [Method 1: Automated PowerShell Launcher (Windows)](#method-1-automated-powershell-launcher-windows)
  - [Method 2: One-Click Batch Script](#method-2-one-click-batch-script)
  - [Method 3: Standard Apache Maven](#method-3-standard-apache-maven)
- [REST API Reference](#-rest-api-reference)
  - [1. Health Check Endpoint](#1-health-check-endpoint)
  - [2. Authentication & OTP Endpoints](#2-authentication--otp-endpoints)
  - [3. Vault File Operations](#3-vault-file-operations)
- [Security & Threat Model](#-security--threat-model)
- [Troubleshooting & FAQs](#-troubleshooting--faqs)

---

## 🌟 Overview & Architecture

When sensitive data is stored on a host operating system, simply deleting or hiding it leaves master file table (MFT) records and raw data on the physical drive sectors. 

The CypherVault Backend operates as an **Anti-Forensic Binary Isolation Broker**:
1. Reads raw file bytes directly into an in-memory buffer.
2. Persists the binary stream into a protected MySQL database as an encrypted `LONGBLOB`.
3. Overwrites file permissions and unlinks (wipes) the source file from the host operating system drive sector to prevent undelete extraction.
4. Enforces time-limited, 6-digit One-Time Passwords (OTP) delivered via TLS-encrypted SMTP for file release and account verification.
5. Safely unhides/restores files back to their exact original path, or streams them directly to the client browser.

```
       [ Client / Frontend ]
                │
                ▼ HTTP / REST (JSON & Multipart)
     ┌────────────────────────────────────────────────────────┐
     │           Spring Boot Application (:8080)              │
     │                                                        │
     │  ┌───────────────────────┐  ┌───────────────────────┐  │
     │  │     AuthController    │  │  VaultFileController  │  │
     │  └──────────┬────────────┘  └───────────┬───────────┘  │
     │             │                           │              │
     │             ▼                           ▼              │
     │  ┌───────────────────────┐  ┌───────────────────────┐  │
     │  │      OtpService       │  │     VaultService      │  │
     │  │ (TTL Cache + SMTP)    │  │ (Encapsulation/Wipe)  │  │
     │  └──────────┬────────────┘  └───────────┬───────────┘  │
     └─────────────┼───────────────────────────┼──────────────┘
                   │                           │
                   ▼                           ▼
        [ Gmail SMTP :587 ]         ┌────────────────────┐
                                    │ MySQL 8.0+ DB      │
                                    │ (`FileHider`)      │
                                    │  - user table      │
                                    │  - vault_files     │
                                    │    (LONGBLOB)      │
                                    └────────────────────┘
                                               │
                                               ▼
                                    [ Host Local Drives ]
                                    (Sector Wipe & Restore)
```

---

## ✨ Key Features

- **Anti-Forensic Local Disk Sanitization**: Ingests files from local Windows/Linux paths (e.g. `C:\Sensitive\TaxDoc.pdf`), encapsulates the binary into MySQL, and wipes the original disk reference.
- **Bi-Directional Unhide Engine**: Reconstructs binary streams back to their exact original directory with original naming and file extensions.
- **Stream/Preview & Download Pipelines**: Allows authenticated users to view (`inline`) or download (`attachment`) files without placing them on the host disk.
- **Dynamic 6-Digit Email OTP Dispatch**: Generates cryptographically secure verification codes with automated asynchronous SMTP delivery.
- **In-Memory TTL Cache**: Temporary OTP codes expire automatically after 5 minutes and are purged immediately upon successful verification.
- **High-Capacity Binary Buffers**: Configured to process files up to **100MB** natively via Spring Multipart configurations.
- **CORS Configured**: Preconfigured cross-origin resource sharing to support the React/Vite development server (`http://localhost:3000`).

---

## 📋 System Requirements

| Prerequisite | Minimum Version | Recommended | Notes |
| :--- | :--- | :--- | :--- |
| **Java Development Kit (JDK)** | 21 LTS | Java 21 or 25 | Required for modern Java records & switch syntax |
| **MySQL Server** | 8.0.0+ | MySQL 8.0 or 8.4 | Must support InnoDB and `max_allowed_packet >= 64M` |
| **Maven** | 3.8.0+ | Maven 3.9+ | Optional if using the built-in PowerShell compiler |
| **SMTP Account** | Gmail or standard SMTP | Gmail App Password | For OTP email delivery |

---

## 💻 Technology Stack

- **Framework**: Spring Boot 3.3.0
- **Language**: Java 21
- **Persistence Framework**: Spring Data JPA / Hibernate 6
- **Database Driver**: MySQL Connector/J 8.3+
- **Security Engine**: Spring Security (Stateless, CSRF disabled for REST, custom CORS)
- **Mailing Engine**: Spring Boot Starter Mail (`JavaMailSender`)
- **Validation**: Jakarta Validation API

---

## 📁 Directory Structure

```
backend/
├── pom.xml                                      # Maven dependencies & build configuration
├── start-backend.bat                            # Windows one-click batch launcher
├── start-backend.ps1                            # Automated compilation & execution script
├── src/
│   └── main/
│       ├── java/com/filehider/
│       │   ├── FileHiderApplication.java        # Spring Boot application entry point
│       │   ├── config/
│       │   │   └── SecurityConfig.java          # Security filter chain & CORS config
│       │   ├── controller/
│       │   │   ├── AuthController.java          # Login, signup, and OTP REST endpoints
│       │   │   ├── HealthController.java        # Health check & database connection probe
│       │   │   └── VaultFileController.java     # Encapsulate, restore, view, download files
│       │   ├── dto/
│       │   │   ├── ApiResponse.java             # Standard JSON API envelope
│       │   │   ├── AuthRequest.java             # Authentication request body
│       │   │   ├── FileResponseDto.java         # Vault file metadata payload
│       │   │   ├── HidePathRequest.java         # Direct path encapsulation payload
│       │   │   ├── SendOtpRequest.java          # OTP request payload
│       │   │   └── UnhideRequest.java           # Unhide/restoration payload
│       │   ├── entity/
│       │   │   ├── User.java                    # Registered user entity
│       │   │   └── VaultFile.java               # Vault file entity with LONGBLOB storage
│       │   ├── repository/
│       │   │   ├── UserRepository.java          # JPA repository for users
│       │   │   └── VaultFileRepository.java     # JPA repository for vault records
│       │   └── service/
│       │       ├── OtpService.java              # OTP generation, cache & SMTP dispatch
│       │       ├── UserService.java             # User registration & lookup
│       │       └── VaultService.java            # Binary ingestion, disk wipe, & restoration
│       └── resources/
│           └── application.properties           # Database, SMTP, and server settings
└── target/                                      # Compiled classes & artifact outputs
```

---

## 🗄️ Database Configuration

1. Ensure MySQL server is running on port `3306`.
2. Connect to MySQL via CLI or Workbench and run:
   ```sql
   CREATE DATABASE IF NOT EXISTS FileHider
     CHARACTER SET utf8mb4
     COLLATE utf8mb4_unicode_ci;
   ```
3. *(Optional)* If handling files larger than 16MB, ensure MySQL's `max_allowed_packet` is sized accordingly in `my.ini` / `my.cnf`:
   ```ini
   [mysqld]
   max_allowed_packet=128M
   ```

---

## ⚙️ Application Properties

File location: `src/main/resources/application.properties`

```properties
# Server Listening Port
server.port=8080

# Spring Application Name
spring.application.name=filehider-backend

# MySQL Database DataSource Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/FileHider?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA & Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# JavaMailSender Configuration (Gmail SMTP)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_16_digit_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.ssl.trust=smtp.gmail.com
spring.mail.properties.mail.smtp.connectiontimeout=10000
spring.mail.properties.mail.smtp.timeout=10000
spring.mail.properties.mail.smtp.writetimeout=10000

# Multipart / File Upload Size Limits
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB

# Logging Configuration
logging.level.com.filehider=INFO
logging.level.org.springframework.web=INFO
```

> [!TIP]
> **Gmail App Password Instructions**:
> 1. Go to your **Google Account** > **Security**.
> 2. Enable **2-Step Verification**.
> 3. Search for **App passwords**.
> 4. Create a new password named "FileHider" and paste the generated 16-character string into `spring.mail.password`.

---

## 🚀 Running the Backend

### Method 1: Automated PowerShell Launcher (Windows)

This repository includes a dedicated script that kills any stale process occupying port `8080`, links `.m2` dependencies, recompiles changed classes, and launches the server:

```powershell
cd backend
.\start-backend.ps1
```

### Method 2: One-Click Batch Script

Double-click `start-backend.bat` or run:
```cmd
cd backend
start-backend.bat
```

### Method 3: Standard Apache Maven

```bash
cd backend
mvn clean spring-boot:run
```

Once running, verify the backend is active at:
👉 **`http://localhost:8080/api/health`**

---

## 🔌 REST API Reference

All responses follow a standard envelope:
```json
{
  "success": true,
  "message": "Operation description",
  "data": { ... }
}
```

### 1. Health Check Endpoint

#### `GET /api/health`
Checks server status and database connectivity.

**Response:**
```json
{
  "status": "UP",
  "service": "CypherVault Anti-Forensic Core",
  "database": "CONNECTED",
  "timestamp": 1725732000000
}
```

---

### 2. Authentication & OTP Endpoints

#### `POST /api/auth/send-otp`
Dispatches a 6-digit OTP code to the requested email.

**Request Body:**
```json
{
  "email": "agent@cyphervault.io",
  "mode": "login", // or "signup"
  "name": "Satwik"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "OTP dispatched successfully to your email: agent@cyphervault.io",
  "data": {
    "email": "agent@cyphervault.io"
  }
}
```

#### `POST /api/auth/login`
Validates the OTP code and grants access to user vault.

**Request Body:**
```json
{
  "email": "agent@cyphervault.io",
  "otp": "481923"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Authentication successful",
  "data": {
    "email": "agent@cyphervault.io",
    "name": "Satwik"
  }
}
```

#### `POST /api/auth/signup`
Creates a new account and verifies email via OTP.

**Request Body:**
```json
{
  "name": "Satwik",
  "email": "agent@cyphervault.io",
  "otp": "481923"
}
```

---

### 3. Vault File Operations

#### `GET /api/files?email={email}`
Fetches all encapsulated files belonging to the specified email account.

**Response (200 OK):**
```json
{
  "files": [
    {
      "id": 1,
      "fileName": "tax_2025.pdf",
      "path": "C:\\Users\\User\\Documents\\tax_2025.pdf",
      "email": "agent@cyphervault.io",
      "size": 1048576
    }
  ]
}
```

#### `POST /api/files/hide-path`
Encapsulates a local file located on the host machine by absolute path, and deletes the local file from disk.

**Request Body:**
```json
{
  "path": "C:\\Users\\SATWIK\\Desktop\\Confidential.docx",
  "email": "agent@cyphervault.io"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "File hidden and wiped from local path",
  "data": {
    "id": 2,
    "fileName": "Confidential.docx",
    "path": "C:\\Users\\SATWIK\\Desktop\\Confidential.docx",
    "email": "agent@cyphervault.io",
    "size": 524288
  }
}
```

#### `POST /api/files/hide` *(Multipart Upload)*
Ingests an uploaded file from the browser into the vault.

**Headers:** `Content-Type: multipart/form-data`  
**Parameters:**
- `file`: Binary file stream
- `email`: User email string
- `path`: *(Optional)* Target restoration path (Defaults to `VaultStorage://upload/<fileName>`)

#### `POST /api/files/unhide`
Extracts the binary payload from MySQL, reconstructs the file back to its original filesystem path on disk, and purges the record from the database.

**Request Body:**
```json
{
  "id": 2
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "File unhidden and removed from vault",
  "data": null
}
```

#### `GET /api/files/download?id={id}&inline={true|false}`
Streams the binary file directly to the client.
- `inline=true`: Opens file in browser tab for inline preview (PDF, images, text).
- `inline=false`: Triggers direct download prompt in browser.

#### `GET /api/files/view?id={id}`
Convenience alias for `inline=true` viewing.

---

## 🔒 Security & Threat Model

1. **Anti-Forensic Sector Overwrite**: When hiding by local path, `VaultService` wipes file attributes and unlinks the target file descriptor.
2. **Ephemeral In-Memory OTP**: Verification codes are strictly held in RAM with a short TTL (5 minutes) and are immediately removed once used.
3. **Database Isolation**: The `bin_data` column stores raw binary blobs, preventing unauthorized local users from opening files via standard explorer tools.
4. **CORS Sanitization**: Explicitly permits cross-origin traffic from trusted frontends while blocking arbitrary external scripting domains in production.

---

## 🛠️ Troubleshooting & FAQs

### Port 8080 is already in use
Run `.\start-backend.ps1` (it automatically terminates orphaned Java processes on port 8080), or manually kill the process in PowerShell:
```powershell
Get-Process -Id (Get-NetTCPConnection -LocalPort 8080).OwningProcess | Stop-Process -Force
```

### Database Connection Refused
- Verify MySQL service is active: `net start mysql` (Windows) or `systemctl status mysql` (Linux).
- Verify credentials in `application.properties` match your MySQL root password.

### Mail Delivery / SMTP Authentication Failed
- Ensure 2-Step Verification is ON in your Google account.
- Generate a dedicated 16-character Google App Password. Normal account passwords will be rejected by Google SMTP.
- Verify outbound port `587` is not blocked by a local firewall.
