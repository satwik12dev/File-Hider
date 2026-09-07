import React from 'react';
import { Lock, Sun, Moon, LogOut, ShieldCheck } from 'lucide-react';
import { Button } from './ui/Button';

export default function Header({
  user,
  onLogout,
  onOpenAuth,
  theme,
  onToggleTheme,
  backendOnline
}) {
  return (
    <header className="minimal-nav">
      {/* Brand */}
      <div className="nav-brand">
        <div className="brand-icon">
          <Lock style={{ width: '17px', height: '17px' }} />
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span className="brand-title">CypherVault</span>
          <span className="brand-tag">v2.4</span>
        </div>
      </div>

      {/* Right Actions */}
      <div className="nav-actions">
        {/* Connection status pill */}
        <div
          className={`status-pill ${backendOnline ? 'online' : 'offline'}`}
          title={backendOnline ? 'Connected to Spring Boot API' : 'Backend offline (local demo active)'}
        >
          <span className="status-dot-pulse" />
          <span>{backendOnline ? 'API Active' : 'Offline'}</span>
        </div>

        {/* Theme Toggle */}
        <button
          type="button"
          onClick={onToggleTheme}
          style={{
            background: 'var(--bg-subtle)',
            border: '1px solid var(--border)',
            borderRadius: '8px',
            padding: '7px 10px',
            cursor: 'pointer',
            color: 'var(--text-secondary)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}
          title={`Switch to ${theme === 'dark' ? 'Light' : 'Dark'} mode`}
        >
          {theme === 'dark' ? (
            <Sun style={{ width: '15px', height: '15px', color: '#facc15' }} />
          ) : (
            <Moon style={{ width: '15px', height: '15px' }} />
          )}
        </button>

        {/* User Session */}
        {user ? (
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                padding: '4px 12px',
                background: 'var(--bg-subtle)',
                border: '1px solid var(--border)',
                borderRadius: '8px',
                fontSize: '13px',
                color: 'var(--text-pure)'
              }}
            >
              <span style={{ fontWeight: '600' }}>{user.name}</span>
            </div>
            <Button
              variant="outline"
              size="sm"
              onClick={onLogout}
              icon={<LogOut style={{ width: '13px', height: '13px' }} />}
              title="Sign Out"
            >
              Lock
            </Button>
          </div>
        ) : (
          <Button
            variant="accent"
            size="sm"
            onClick={() => onOpenAuth('login')}
            icon={<Lock style={{ width: '13px', height: '13px' }} />}
          >
            Access Vault
          </Button>
        )}
      </div>
    </header>
  );
}
