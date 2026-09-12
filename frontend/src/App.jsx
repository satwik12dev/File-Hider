import React, { useState, useEffect, useCallback } from 'react';
import Header from './components/Header';
import SecurityHUD from './components/SecurityHUD';
import GuestHero from './components/GuestHero';
import Dashboard from './components/Dashboard';
import AuthModal from './components/AuthModal';
import PreviewModal from './components/PreviewModal';
import ConfirmUnhideModal from './components/ConfirmUnhideModal';
import CyberSecurityBackground from './components/CyberSecurityBackground';
import { ToastContainer } from './components/ui/Toast';
import { checkBackendHealth, fetchVaultFiles, unhideFileApi, getFileDownloadUrl, getStoredToken } from './services/api';

export default function App() {
  const [user, setUser] = useState(() => {
    try {
      const saved = localStorage.getItem('cyphervault_user');
      return saved ? JSON.parse(saved) : null;
    } catch {
      return null;
    }
  });

  const [theme, setTheme] = useState(() => {
    return localStorage.getItem('cyphervault_theme') || 'dark';
  });

  const [backendOnline, setBackendOnline] = useState(false);
  const [files, setFiles] = useState([]);
  const [loadingFiles, setLoadingFiles] = useState(false);

  const [authModal, setAuthModal] = useState({ open: false, mode: 'login' });
  const [previewFile, setPreviewFile] = useState(null);
  const [unhideTarget, setUnhideTarget] = useState(null);
  const [unhideLoading, setUnhideLoading] = useState(false);
  const [toasts, setToasts] = useState([]);

  const showToast = useCallback((message, type = 'info') => {
    const id = Date.now() + Math.random();
    setToasts((prev) => [...prev, { id, message, type }]);
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, 4000);
  }, []);

  const dismissToast = (id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  };

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('cyphervault_theme', theme);
  }, [theme]);

  const toggleTheme = () => {
    setTheme((prev) => (prev === 'dark' ? 'light' : 'dark'));
  };

  useEffect(() => {
    const check = async () => {
      const res = await checkBackendHealth();
      setBackendOnline(res.online);
    };
    check();
    const timer = setInterval(check, 3500);
    return () => clearInterval(timer);
  }, []);

  const loadFiles = useCallback(async () => {
    if (!user) {
      setFiles([]);
      return;
    }

    setLoadingFiles(true);
    try {
      const data = await fetchVaultFiles(user.email);
      setFiles(data);
    } catch {
      setFiles([]);
    } finally {
      setLoadingFiles(false);
    }
  }, [user]);

  useEffect(() => {
    if (user) {
      loadFiles();
    }
  }, [user, loadFiles]);

  const handleAuthSuccess = (userData) => {
    setUser(userData);
    localStorage.setItem('cyphervault_user', JSON.stringify(userData));
  };

  const handleLogout = () => {
    setUser(null);
    setFiles([]);
    localStorage.removeItem('cyphervault_user');
    showToast('Vault session locked', 'info');
  };

  const handleConfirmUnhide = async () => {
    if (!unhideTarget) return;
    setUnhideLoading(true);
    try {
      await unhideFileApi(unhideTarget.id);
      showToast(`Restored "${unhideTarget.fileName}"`, 'success');
      setUnhideTarget(null);
      await loadFiles();
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setUnhideLoading(false);
    }
  };

  return (
    <div style={{ minHeight: '100vh', position: 'relative', overflowX: 'hidden' }}>
      {/* Subtle Ambient 3D Depth Canvas */}
      {/* Cyber Security Background with Animated Matrix, Grid & Radar */}
      <CyberSecurityBackground />

      {/* Top Soft Radial Accent */}
      <div className="minimal-radial-gradient" />

      {/* Main Container */}
      <div className="app-container">
        {/* Navigation */}
        <Header
          user={user}
          onLogout={handleLogout}
          onOpenAuth={(mode) => setAuthModal({ open: true, mode })}
          theme={theme}
          onToggleTheme={toggleTheme}
          backendOnline={backendOnline}
        />

        {/* Real-Time Military Security HUD Status Bar */}
        <SecurityHUD />

        {/* View Toggle */}
        <main style={{ flex: 1 }}>
          {user ? (
            <Dashboard
              user={user}
              files={files}
              onRefreshFiles={loadFiles}
              onOpenPreview={(file) => setPreviewFile(file)}
              onPromptUnhide={(file) => setUnhideTarget(file)}
              onShowToast={showToast}
            />
          ) : (
            <GuestHero onOpenAuth={(mode) => setAuthModal({ open: true, mode })} />
          )}
        </main>

        {/* Minimal Clean Footer */}
        <footer className="minimal-footer">
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: 'var(--status-live)' }} />
            <span>CypherVault • AES-256 Client-Side Enclave</span>
          </div>
          <div>
            Built with React, Vite & Three.js
          </div>
        </footer>
      </div>

      {/* Modals */}
      <AuthModal
        isOpen={authModal.open}
        initialMode={authModal.mode}
        onClose={() => setAuthModal({ open: false, mode: 'login' })}
        onAuthSuccess={handleAuthSuccess}
        onShowToast={showToast}
      />

      <PreviewModal
        file={previewFile}
        isOpen={!!previewFile}
        onClose={() => setPreviewFile(null)}
        onUnhide={(file) => setUnhideTarget(file)}
        onDownload={(file) => {
          const cleanName = (file.fileName || 'decrypted_file').replace(/["']/g, '');
          const token = getStoredToken();
          if (token && file.id < 100) {
            window.location.href = getFileDownloadUrl(file.id, false);
          } else {
            const blob = new Blob([`[Decrypted CypherVault Asset]\nFile: ${cleanName}\nOriginal Path: ${file.path || 'Vault Enclave'}\nEncryption: AES-256-GCM authenticated cipher`], { type: 'text/plain;charset=utf-8' });
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = cleanName;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            URL.revokeObjectURL(url);
          }
          showToast(`Downloading ${cleanName}...`, 'info');
        }}
      />

      <ConfirmUnhideModal
        file={unhideTarget}
        isOpen={!!unhideTarget}
        onClose={() => setUnhideTarget(null)}
        onConfirm={handleConfirmUnhide}
        loading={unhideLoading}
      />

      {/* Toast Notifications */}
      <ToastContainer toasts={toasts} onDismiss={dismissToast} />
    </div>
  );
}
