# CypherVault (`File-Hider`)

A full-stack encrypted file vault that conceals local or uploaded files using **AES-256-GCM** encryption, stores encrypted payloads in **MySQL (`LONGBLOB`)**, securely wipes the original file from disk, and restores or streams files on demand behind **Email OTP + JWT** authentication.

---

## How It Works

```
React / Vite (:3000) ──(/api proxy)──▶ Spring Boot API (:8080) ──▶ MySQL (FileHider)
                                                │
                                                ├──▶ Gmail SMTP :465 (4-digit OTP)
                                                └──▶ Host Disk (Zero-wipe / Restore)
```

1. **Authentication (OTP + JWT)**
   - Users request a **4-digit OTP** (`SecureRandom`, 10-minute TTL, 60-second resend cooldown, max 5 failed attempts) sent asynchronously via Gmail SMTP (`JavaMailSender`), with console/API debug fallback if mail is unconfigured.
   - Verifying the OTP at login or signup issues a stateless **HMAC-SHA256 JWT** (24-hour expiration by default).

2. **Conceal (`hide` / `hide-path`)**
   - **By Path (`/api/files/hide-path`)**: Reads a file from the local filesystem (resolving absolute paths or searching common user folders like Desktop, Documents, Downloads), blocks OS-critical paths (`\Windows`, `\Program Files`, `\etc`, `\bin`, root drives), encrypts bytes with **AES-256-GCM** (unique 12-byte random IV + SHA-256 plaintext checksum), saves to MySQL, clears Windows attributes (`attrib -r -s -h`), overwrites file sectors with zeros via `RandomAccessFile("rws")` + `FD.sync()`, and deletes the file from disk.
   - **By Upload (`/api/files/hide`)**: Accepts multipart uploads up to **100 MB**, encrypts in memory, persists in MySQL, and wipes a matching local file if found.

3. **Restore & Preview (`unhide` / `download` / `view`)**
   - **Unhide (`/api/files/unhide`)**: Decrypts the payload, writes the file back to its original disk path (or Desktop/Downloads for `VaultStorage://` uploads), and deletes the database record.
   - **Download / View (`/api/files/download`, `/api/files/view`)**: Decrypts on the fly and streams directly to the browser as `attachment` or `inline` preview.

---

## Tech Stack

| Layer | Technologies |
| :--- | :--- |
| **Backend** | Java 24, Spring Boot 3.4.0, Spring Security, Spring Data JPA (Hibernate), JJWT 0.12.6, SpringDoc OpenAPI 2.8.5, Spring Boot Actuator |
| **Database** | MySQL 8+ (HikariCP pool, `FileHider` schema), H2 (tests) |
| **Frontend** | React 19, Vite 8, Three.js (`@react-three/fiber`, `@react-three/drei`), Lucide React, Canvas Confetti |

---

## Project Structure

```text
File-Hider/
├── backend/
│   ├── pom.xml                        # Maven build & dependencies (Spring Boot 3.4, Java 24)
│   ├── .env.example                   # Environment variable template
│   ├── start-backend.ps1 / .bat       # Windows compile & launch scripts (frees port 8080)
│   └── src/
│       ├── main/java/com/filehider/
│       │   ├── config/                # SecurityConfig, CorsConfig, AsyncConfig, OpenApiConfig, WebStaticConfig
│       │   ├── controller/            # AuthController, VaultFileController, HealthController
│       │   ├── dto/                   # Request/response DTOs & ApiResponse wrapper
│       │   ├── entity/                # User (`user`), VaultFile (`data`)
│       │   ├── exception/             # GlobalExceptionHandler, ResourceNotFoundException
│       │   ├── repository/            # UserRepository, VaultFileRepository
│       │   ├── security/              # AesEncryptionService, JwtService, JwtAuthenticationFilter
│       │   └── service/               # OtpService, UserService, VaultService
│       ├── main/resources/
│       │   └── application.properties
│       └── test/java/com/filehider/   # Unit tests (AES, JWT, OTP, Vault)
└── frontend/
    ├── package.json
    ├── vite.config.js                 # Dev server on :3000, proxies /api -> http://127.0.0.1:8080
    └── src/
        ├── App.jsx                    # Root state (auth, theme, health polling, modals)
        ├── services/
        │   ├── api.js                 # REST client + automatic LocalStorage Offline Demo fallback
        │   └── cyberAudio.js          # Web Audio UI sound effects
        └── components/
            ├── AuthModal.jsx          # Login/Signup flow with 4-digit OTP input
            ├── Dashboard.jsx          # Hide by path/upload, search, category filters, grid/list view
            ├── PreviewModal.jsx       # In-app preview (images, PDF, audio, video, text/code)
            ├── ConfirmUnhideModal.jsx # Restore confirmation dialog
            ├── SecurityHUD.jsx        # Vault file count & storage summary
            ├── ThreeVaultScene.jsx    # Interactive 3D WebGL vault graphic
            ├── ThreeParticleCanvas.jsx & CyberSecurityBackground.jsx
            ├── ConcealmentPipeline.jsx & VaultScannerReticle.jsx
            ├── GuestHero.jsx & Header.jsx
            └── ui/                    # Badge, Button, Card, Input, Toast
```

---

## Setup & Running

### Prerequisites

- **JDK 24** (or compatible JDK matching `backend/pom.xml`)
- **MySQL 8+** running on `localhost:3306`
- **Node.js 18+** and **npm**
- **Maven 3.8+** (optional if using `backend/start-backend.ps1`)

### 1. Configure Backend Environment

Create `backend/.env` (or export variables / edit `backend/src/main/resources/application.properties`):

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

> **Notes:**
> - `spring.jpa.hibernate.ddl-auto=update` creates/updates the `user` and `data` tables automatically.
> - Gmail SMTP uses SSL on port `465` and requires a Google **App Password** (with 2-Step Verification enabled). If email sending fails, the backend logs the OTP to the console.
> - For files larger than 16 MB in MySQL, increase `max_allowed_packet` (e.g., `128M`) in MySQL config.

### 2. Start the Backend

**Windows (PowerShell script):**
```powershell
cd backend
.\start-backend.ps1
```
*(Or double-click `backend/start-backend.bat`.)*

**Maven CLI (cross-platform):**
```bash
cd backend
mvn spring-boot:run
```

Backend runs at `http://localhost:8080`.

### 3. Start the Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at `http://localhost:3000` and proxies `/api/*` requests to `http://127.0.0.1:8080`.

**Other frontend commands:**
```bash
npm run build    # Production bundle
npm run preview  # Preview production build
npm run lint     # Run oxlint
```

**Backend tests:**
```bash
cd backend
mvn test
```

---

## Useful URLs

| Resource | URL |
| :--- | :--- |
| Web UI | `http://localhost:3000` |
| Health Check | `http://localhost:8080/api/health` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| Actuator Health | `http://localhost:8080/actuator/health` |

---

## REST API Reference

Protected endpoints require `Authorization: Bearer <token>` (or `?token=<token>` query parameter for browser media/download links).

### Health & Auth (Public)

| Method | Endpoint | Body / Params | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/health` | — | Returns backend `status` (`UP`), framework, and timestamp |
| `POST` | `/api/auth/send-otp` | `{ email, mode: "login" \| "signup", name? }` | Generates & emails a 4-digit OTP (60s cooldown) |
| `POST` | `/api/auth/login` | `{ email, otp }` | Validates OTP and returns `{ email, name, token }` |
| `POST` | `/api/auth/signup` | `{ name?, email, otp }` | Validates OTP, creates user, returns `{ email, name, token }` |

### Vault Files (Authenticated)

| Method | Endpoint | Body / Params | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/files` | `?email=` (optional; enforced against JWT principal) | Lists user's vaulted file metadata |
| `POST` | `/api/files/hide` | `multipart/form-data`: `file`, `email`, `path?` | Encrypts uploaded file and stores it in vault |
| `POST` | `/api/files/hide-path` | `{ path, email }` | Encrypts local file at `path`, saves to vault, wipes original |
| `POST` | `/api/files/unhide` | `{ id }` | Decrypts file, restores to disk, removes vault entry |
| `GET` | `/api/files/download` | `?id=<id>&inline=<true\|false>` | Streams decrypted file (`attachment` or `inline`) |
| `GET` | `/api/files/view` | `?id=<id>` | Alias for `/api/files/download?inline=true` |

---

## Database Schema

### `user`

| Column | Type | Notes |
| :--- | :--- | :--- |
| `id` | `INT` (PK, auto-increment) | User ID |
| `name` | `VARCHAR` (not null) | Display name |
| `email` | `VARCHAR` (not null, unique) | Indexed (`idx_user_email`) |
| `created_at` / `updated_at` | `DATETIME` | Hibernate timestamps |

### `data`

| Column | Type | Notes |
| :--- | :--- | :--- |
| `id` | `INT` (PK, auto-increment) | Vault file ID |
| `nameoffile` | `VARCHAR` (not null) | Original file name |
| `path` | `VARCHAR` | Original absolute disk path or `VaultStorage://<name>` |
| `email` | `VARCHAR` (not null) | Owner email, indexed (`idx_vault_email`) |
| `bin_data` | `LONGBLOB` | AES-256-GCM ciphertext |
| `iv` | `VARBINARY` / `BLOB` | 12-byte GCM initialization vector |
| `checksum` | `VARCHAR(64)` | SHA-256 hex digest of plaintext |
| `content_type` | `VARCHAR` | Detected MIME type |
| `file_size` | `BIGINT` | Original plaintext size in bytes |
| `created_at` / `updated_at` | `DATETIME` | Hibernate timestamps |

---

## Frontend Features & Offline Demo Mode

- **Offline Demo Fallback**: If the Spring Boot backend is unreachable, `frontend/src/services/api.js` automatically simulates auth and vault operations in browser `localStorage` (`cyphervault_demo_files_v2`). Once `/api/health` responds `UP`, the header status switches online.
- **Dark / Light Theme**: Toggled from the header and persisted in `localStorage` (`cyphervault_theme`).
- **Vault Management**: Search by filename/path, filter by category (`image`, `doc`, `media`, `code`, `archive`, `other`), switch between grid/list views, and preview images, PDFs, audio, video, and source/text files directly in the browser.

---

## Security Notes

- **Production Secrets**: Override default `VAULT_MASTER_KEY` and `JWT_SECRET` values before deploying; the AES-256 key is derived via SHA-256 of `vault.security.master-key`.
- **OTP Debug Field**: `AuthController.sendOtp` currently includes the generated `otp` in the JSON response (`ApiResponse.withOtp`) for local testing convenience; remove `.withOtp(otp)` for strict production deployments.
- **Disk Wipe Scope**: `VaultService.wipeFileFromDisk` performs a single-pass zero overwrite and OS unlink. SSD wear-leveling or cloud-synced folders (like OneDrive) may retain prior copies outside the application's control.
