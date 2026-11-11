# CypherVault Backend

Spring Boot REST API for **CypherVault** (`File-Hider`): handles 4-digit email OTP authentication, JWT issuance, AES-256-GCM encryption, MySQL `LONGBLOB` persistence, and secure disk wiping/restoration.

---

## Stack

- **Java 24** / **Spring Boot 3.4.0**
- **Spring Security** + **JJWT 0.12.6** (stateless HMAC-SHA256 JWT)
- **Spring Data JPA (Hibernate)** + **MySQL 8+** (`HikariCP`), **H2** for tests
- **JavaMailSender** (async Gmail SMTP SSL on port `465`)
- **SpringDoc OpenAPI 2.8.5** + **Spring Boot Actuator**

---

## Structure

```text
backend/
├── pom.xml
├── .env.example
├── start-backend.ps1 / start-backend.bat
└── src/
    ├── main/java/com/filehider/
    │   ├── FileHiderApplication.java
    │   ├── config/       # AsyncConfig, CorsConfig, OpenApiConfig, SecurityConfig, WebStaticConfig
    │   ├── controller/   # AuthController, HealthController, VaultFileController
    │   ├── dto/          # ApiResponse, AuthRequest, FileResponseDto, HidePathRequest, SendOtpRequest, UnhideRequest
    │   ├── entity/       # User (`user`), VaultFile (`data`)
    │   ├── exception/    # GlobalExceptionHandler, ResourceNotFoundException
    │   ├── repository/   # UserRepository, VaultFileRepository
    │   ├── security/     # AesEncryptionService, JwtService, JwtAuthenticationFilter
    │   └── service/      # OtpService, UserService, VaultService
    ├── main/resources/application.properties
    └── test/java/com/filehider/
```

---

## Configuration

Copy `.env.example` to `.env` or configure environment variables / `src/main/resources/application.properties`:

```env
PORT=8080
DB_URL=jdbc:mysql://localhost:3306/FileHider?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_16_char_gmail_app_password
VAULT_MASTER_KEY=change-this-to-a-strong-secret-key
JWT_SECRET=change-this-to-a-256-bit-or-longer-secret-key
JWT_EXPIRATION_MS=86400000
```

- `spring.jpa.hibernate.ddl-auto=update` manages the `user` and `data` tables automatically.
- Max multipart upload size is `100MB` (ensure MySQL `max_allowed_packet` is sized accordingly for large files).
- Gmail SMTP requires 2-Step Verification and a 16-character App Password. If mail fails or is unconfigured, the OTP is logged to the console.

---

## Run & Test

**Windows PowerShell launcher** (releases port `8080`, compiles with `javac --release 21` against `.m2`, and starts the app):
```powershell
.\start-backend.ps1
```

**Maven CLI:**
```bash
mvn spring-boot:run
```

**Run unit tests (H2 in-memory DB):**
```bash
mvn test
```

**Endpoints once running (`http://localhost:8080`):**
- Health: `GET /api/health` and `GET /actuator/health`
- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/v3/api-docs`

---

## Core Behavior

- **OTP (`OtpService`)**: 4-digit `SecureRandom` code, 10-minute validity, 60-second cooldown per email, invalidated after 5 failed attempts or first successful use.
- **Encryption (`AesEncryptionService`)**: Derives a 256-bit AES key via SHA-256 of `vault.security.master-key`, encrypts with `AES/GCM/NoPadding` using a fresh 12-byte IV, and stores the SHA-256 plaintext checksum.
- **Disk Sanitization (`VaultService`)**: Blocks OS system directories (`\Windows`, `\Program Files`, `\etc`, `\bin`, root drives), clears `attrib -r -s -h` on Windows, zero-overwrites via `RandomAccessFile("rws")` + `sync()`, and unlinks the file.
- **Auth Guard (`SecurityConfig` & `JwtAuthenticationFilter`)**: `/api/auth/**`, `/api/health`, `/actuator/**`, and `/swagger-ui/**` are public; `/api/files/**` requires a valid JWT via `Authorization: Bearer <token>` or `?token=<token>`.
