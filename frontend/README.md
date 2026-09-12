<div align="center">

# 🖥️ CypherVault - Frontend Tactical HUD
### *React 19 • Vite 6 • Three.js / React Three Fiber • Dual Theme Engine • Cyberpunk UI*

[![React 19](https://img.shields.io/badge/React-19.2-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-6.x%20%2F%208.x-646CFF?style=for-the-badge&logo=vite&logoColor=white)](https://vitejs.dev/)
[![Three.js](https://img.shields.io/badge/Three.js-WebGL-black?style=for-the-badge&logo=threedotjs&logoColor=white)](https://threejs.org/)
[![Lucide Icons](https://img.shields.io/badge/Lucide-Icons-F56565?style=for-the-badge&logo=lucide&logoColor=white)](https://lucide.dev/)
[![Theme Matrix](https://img.shields.io/badge/Theme-Light%20%26%20Dark-22c55e?style=for-the-badge)](#-theme-system)

<br/>

<p align="center">
  The <b>CypherVault Frontend</b> is an ultra-modern, high-performance tactical interface built with React 19 and Three.js. It delivers real-time encrypted file operations, 3D holographic vault visualization, inline file previewing, and seamless REST integration with the Spring Boot backend.
</p>

</div>

---

## 📑 Table of Contents

- [Features & Highlights](#-features--highlights)
- [Architecture & Dataflow](#-architecture--dataflow)
- [Technology Stack](#-technology-stack)
- [Directory Structure](#-directory-structure)
- [Component Breakdown](#-component-breakdown)
- [Theme System (Light & Dark Matrix)](#-theme-system)
- [API Integration & Offline Enclave](#-api-integration--offline-enclave)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Development Server](#development-server)
  - [Production Build](#production-build)
- [Vite Proxy Configuration](#-vite-proxy-configuration)
- [Keyboard Shortcuts & Ergonomics](#-keyboard-shortcuts--ergonomics)
- [Troubleshooting](#-troubleshooting)

---

## ✨ Features & Highlights

- **🧊 3D Holographic Vault (Three.js & R3F)**:
  - Interactive WebGL 3D vault orb rendered with dynamic geometry, floating glowing particle field, and ambient rotation.
  - Interactive mouse tracking and theme-reactive lighting effects.

- **🗂️ Dual File Ingestion Matrix**:
  - **Direct Path Encapsulation**: Input local machine absolute paths (e.g., `C:\Secret\Doc.pdf`) to command the backend to encapsulate the binary and sanitize original drive sectors.
  - **Browser Drag-and-Drop**: Multi-file drop zone for instant browser-based uploads into database storage.

- **👁️ In-App Multi-Format Previewer**:
  - Live preview modal for images (`.png`, `.jpg`, `.svg`), PDFs, audio, code, and documents directly from memory streams without downloading to disk.

- **🔐 6-Digit Email OTP Authentication**:
  - Polished authentication modal supporting both account registration and login.
  - Asynchronous OTP trigger, visual 60-second countdown resend timer, and PIN input auto-advance.

- **🌓 Dual-Engine Theme Matrix**:
  - **Obsidian Dark Matrix**: Tactical deep obsidian background (`#090d16`) with cyberpunk neon emerald accents (`#22c55e`).
  - **White Clean Mode**: Modern, ultra-crisp slate palette (`#f8fafc`) inspired by Shadcn UI.
  - Persistent theme memory in `localStorage` with system preference auto-detection.

- **📊 Live Telemetry & Quick Filtering**:
  - Real-time computing of total storage saved, asset count, and category segmentation (Docs, Media, Code, Archives).
  - Search bar with instant fuzzy filtering by filename or origin directory.

- **🛡️ Graceful Offline Enclave Fallback**:
  - If the Spring Boot backend is offline or starting up, the frontend automatically falls back to an offline simulated enclave with mock OTP generation for uninterrupted UI evaluation.

---

## 🏗️ Architecture & Dataflow

```
   ┌────────────────────────────────────────────────────────┐
   │                  React 19 Application                  │
   │                                                        │
   │  ┌──────────────────────────────────────────────────┐  │
   │  │             App.jsx (Root State Hub)             │  │
   │  │  - Auth state (user, email, authenticated)       │  │
   │  │  - Theme state (light / dark)                    │  │
   │  │  - Backend live health monitor                   │  │
   │  └────────┬────────────────────────────────┬────────┘  │
   │           │                                │           │
   │           ▼                                ▼           │
   │  ┌─────────────────┐              ┌─────────────────┐  │
   │  │  GuestHero.jsx  │              │  Dashboard.jsx  │  │
   │  │ (Unauthenticated│              │  (Full Vault    │  │
   │  │  Landing State) │              │   Telemetry)    │  │
   │  └────────┬────────┘              └────────┬────────┘  │
   │           │                                │           │
   │           ▼                                ▼           │
   │  ┌─────────────────┐              ┌─────────────────┐  │
   │  │  AuthModal.jsx  │              │ PreviewModal.jsx│  │
   │  │  (OTP / Login)  │              │ (Inline Viewer) │  │
   │  └─────────────────┘              └─────────────────┘  │
   │           │                                │           │
   │           └───────────────┬────────────────┘           │
   │                           ▼                            │
   │             ┌───────────────────────────┐              │
   │             │   api.js Service Layer    │              │
   │             └─────────────┬─────────────┘              │
   └───────────────────────────┼────────────────────────────┘
                               │
                               ▼ Vite Proxy (/api -> :8080)
                [ Spring Boot Backend (:8080) ]
```

---

## 💻 Technology Stack

- **Core Library**: [React 19](https://react.dev/)
- **Build Tool / Bundler**: [Vite](https://vitejs.dev/) (Fast HMR & ESM native bundling)
- **3D Graphics**:
  - [Three.js](https://threejs.org/) (r185)
  - [@react-three/fiber](https://r3f.docs.pmnd.rs/) (React renderer for Three.js)
  - [@react-three/drei](https://github.com/pmndrs/drei) (Useful R3F helpers & primitives)
- **Iconography**: [Lucide React](https://lucide.dev/) (Modern vector SVG icon suite)
- **Visual FX**: [canvas-confetti](https://www.npmjs.com/package/canvas-confetti) (Unlock and restore victory animations)
- **Styling**: Vanilla CSS3 Custom Properties Design System with glassmorphic cards and glowing HUD borders
- **Linter**: [Oxlint](https://oxc.rs/)

---

## 📁 Directory Structure

```
frontend/
├── index.html                                   # Root HTML document & viewport meta
├── vite.config.js                               # Vite config & API reverse proxy (:8080)
├── package.json                                 # Scripts and npm dependencies
├── .oxlintrc.json                               # Fast Rust-based linter rules
├── public/                                      # Static public assets & favicons
└── src/
    ├── main.jsx                                 # React root bootstrap & DOM mount
    ├── App.jsx                                  # Main application controller & state
    ├── App.css                                  # Layout containers & responsive rules
    ├── index.css                                # Design tokens, theme variables & animations
    ├── components/
    │   ├── Header.jsx                           # Top HUD navbar, theme switcher, auth badge
    │   ├── GuestHero.jsx                        # Showcase landing page for logged-out visitors
    │   ├── Dashboard.jsx                        # Main vault view, search, upload & action grid
    │   ├── AuthModal.jsx                        # OTP login & user registration popup
    │   ├── PreviewModal.jsx                     # In-browser multimedia & document viewer
    │   ├── ConfirmUnhideModal.jsx               # Safety confirmation dialog before restoring
    │   ├── ThreeVaultScene.jsx                  # 3D interactive holographic vault core
    │   └── ThreeParticleCanvas.jsx              # Ambient floating particle field background
    └── services/
        └── api.js                               # Unified REST client & offline fallback cache
```

---

## 🧩 Component Breakdown

| Component | Responsibility |
| :--- | :--- |
| **`Header.jsx`** | Displays the CypherVault logo, live backend health badge (Online/Demo), theme toggle button, and authenticated user profile with logout. |
| **`GuestHero.jsx`** | Displays introductory security capabilities, 3D animated vault showcase, and "Access Security Enclave" action button. |
| **`Dashboard.jsx`** | Central command deck: Live telemetry counters (Total files, memory footprint, breakdown), path encapsulation form, drag-and-drop dropzone, and file catalog. |
| **`ThreeVaultScene.jsx`** | WebGL 3D holographic orb running via React Three Fiber with ambient floating particles, dynamic rotation, and responsive canvas sizing. |
| **`AuthModal.jsx`** | Two-step authentication: 1. Send OTP to email; 2. Enter 6-digit code with auto-focus, countdown timer, and automatic submit. |
| **`PreviewModal.jsx`** | Renders inline previews for images, text, and PDFs fetched from `/api/files/download?inline=true` without leaking data to host drive. |
| **`ConfirmUnhideModal.jsx`**| Prevents accidental restores by displaying original target path before issuing an unhide command to disk. |

---

## 🌓 Theme System

The design system is managed via CSS Custom Properties in [`src/index.css`](file:///c:/Users/SATWIK/OneDrive/Desktop/FileHiderApp/frontend/src/index.css).

```css
/* Dark Obsidian Matrix (Default) */
[data-theme='dark'] {
  --bg-primary: #090d16;
  --bg-card: rgba(15, 23, 42, 0.75);
  --accent-cyan: #22c55e; /* Cyber Green */
  --text-primary: #f8fafc;
  --border-color: rgba(34, 197, 94, 0.2);
}

/* Crisp Clean Light Mode */
[data-theme='light'] {
  --bg-primary: #f8fafc;
  --bg-card: #ffffff;
  --accent-cyan: #16a34a;
  --text-primary: #0f172a;
  --border-color: rgba(226, 232, 240, 0.8);
}
```

- **Theme Persistence**: Synced in `localStorage` under `'cyphervault-theme'`.
- **System Preference**: Respects `(prefers-color-scheme: dark)` out of the box.

---

## 🔌 API Integration & Offline Enclave

All network calls are centralized in [`src/services/api.js`](file:///c:/Users/SATWIK/OneDrive/Desktop/FileHiderApp/frontend/src/services/api.js):

- **`checkBackendHealth()`** - Queries `GET /api/health` with a 2-second timeout to determine if Spring Boot is reachable.
- **`sendOtpApi({ email, mode, name })`** - Dispatches request to `POST /api/auth/send-otp`.
- **`loginApi({ email, otp })`** - Authenticates user credentials against `POST /api/auth/login`.
- **`fetchFilesApi(email)`** - Retrieves user's encrypted file registry from `GET /api/files?email={email}`.
- **`hideLocalPathApi({ path, email })`** - Sends absolute file path to `POST /api/files/hide-path`.
- **`hideUploadedFileApi({ file, email, customPath })`** - Uploads multipart file to `POST /api/files/hide`.
- **`unhideFileApi(id)`** - Requests disk restoration via `POST /api/files/unhide`.

### 🛡️ Built-in Offline Fallback
If the backend is not yet started or temporarily unreachable:
- Health monitor displays amber status: `DEMO ENCLAVE`.
- OTP generator generates a client-side mock code and displays it in a toast notification so you can log in and test all dashboard interactions immediately.
- Files hidden in demo mode persist inside your browser's `localStorage` (`cyphervault_demo_files_v2`).

---

## 🚀 Getting Started

### Prerequisites
- **Node.js**: Version 18.x or 20.x+ installed.
- **npm**: Comes bundled with Node.js.

### Installation
Navigate to the `frontend` folder and install dependencies:
```bash
cd frontend
npm install
```

### Development Server
Run the local Vite development server with Hot Module Replacement (HMR):
```bash
npm run dev
```
Open your browser at:
👉 **`http://localhost:3000`**

### Production Build
Compile and optimize client assets for production:
```bash
npm run build
```
Preview the compiled production bundle:
```bash
npm run preview
```

---

## 🔄 Vite Proxy Configuration

Configured in [`vite.config.js`](file:///c:/Users/SATWIK/OneDrive/Desktop/FileHiderApp/frontend/vite.config.js):

```javascript
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    host: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      }
    }
  }
})
```

Any HTTP request to `/api/*` from `http://localhost:3000` is automatically proxied to `http://localhost:8080` without triggering CORS issues in development.

---

## ⌨️ Keyboard Shortcuts & Ergonomics

- `ESC` - Closes any active modal (Preview, OTP Login, Unhide Confirmation).
- `Enter` - Submits the active path hide form or OTP verification code.
- `Tab` - Seamless keyboard navigation across inputs and interactive elements.

---

## 🛠️ Troubleshooting

### Vite port 3000 is occupied
Vite will automatically offer an alternate port (e.g., `3001`). You can change the port in `vite.config.js` under `server.port`.

### 3D Canvas displays a black box or does not load
- Ensure WebGL is enabled in your browser settings (*Settings > System > Use graphics acceleration when available*).
- Verify modern GPU driver support.

### Backend Health shows "DEMO ENCLAVE"
- Check that the Spring Boot backend is running on port 8080 (`.\start-backend.ps1` in `backend/`).
- Visit `http://localhost:8080/api/health` directly in your browser to check if Spring Boot is responding.
