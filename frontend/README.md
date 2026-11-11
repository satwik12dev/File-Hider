# CypherVault Frontend

React 19 + Vite 8 web interface for **CypherVault**, featuring a 3D Three.js vault scene, 4-digit OTP authentication, file hiding by path or upload, in-browser previews, and an automatic `localStorage` offline demo mode.

---

## Stack

- **React 19** + **Vite 8**
- **Three.js** (`@react-three/fiber`, `@react-three/drei`)
- **Lucide React**, **Canvas Confetti**, **Oxlint**
- Vanilla CSS design tokens (`src/index.css`) with dark/light themes

---

## Structure

```text
frontend/
├── index.html
├── package.json
├── vite.config.js          # Port 3000, proxies /api -> http://127.0.0.1:8080
└── src/
    ├── main.jsx
    ├── App.jsx             # Auth/user state, theme toggle, /api/health polling, modals
    ├── index.css / App.css
    ├── services/
    │   ├── api.js          # REST client + offline localStorage demo fallback
    │   └── cyberAudio.js   # Web Audio UI sound effects
    └── components/
        ├── Header.jsx
        ├── GuestHero.jsx
        ├── Dashboard.jsx          # Hide by path/upload, search, category filter, grid/list view
        ├── AuthModal.jsx          # Login/signup with 4-digit OTP input
        ├── PreviewModal.jsx       # Inline preview (images, PDF, audio, video, text/code)
        ├── ConfirmUnhideModal.jsx # Restore confirmation dialog
        ├── SecurityHUD.jsx        # Vault file count & storage summary
        ├── ThreeVaultScene.jsx    # 3D WebGL vault graphic
        ├── ThreeParticleCanvas.jsx
        ├── CyberSecurityBackground.jsx
        ├── ConcealmentPipeline.jsx
        ├── VaultScannerReticle.jsx
        └── ui/                    # Badge, Button, Card, Input, Toast
```

---

## Commands

```bash
npm install      # Install dependencies
npm run dev      # Start dev server at http://localhost:3000
npm run build    # Build production bundle
npm run preview  # Preview production build
npm run lint     # Lint with oxlint
```

---

## Key Details

- **API Proxy**: `vite.config.js` forwards `/api/*` requests to `http://127.0.0.1:8080` (returning HTTP `503` JSON when the backend is offline).
- **Offline Demo Mode**: `src/services/api.js` polls `/api/health` and falls back to `localStorage` (`cyphervault_demo_files_v2`) when the backend is unreachable so the UI remains testable without MySQL/Spring Boot running.
- **Theme Persistence**: Dark/light theme state is stored under `localStorage` key `cyphervault_theme` via `data-theme` on `<html>`.
