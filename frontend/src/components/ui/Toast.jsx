import React from 'react';
import { CheckCircle2, AlertCircle, Info, X } from 'lucide-react';

export function ToastContainer({ toasts, onDismiss }) {
  if (!toasts || toasts.length === 0) return null;

  const getIcon = (type) => {
    switch (type) {
      case 'success':
        return <CheckCircle2 style={{ width: '15px', height: '15px', color: 'var(--status-live)', flexShrink: 0 }} />;
      case 'error':
        return <AlertCircle style={{ width: '15px', height: '15px', color: 'var(--status-danger)', flexShrink: 0 }} />;
      default:
        return <Info style={{ width: '15px', height: '15px', color: 'var(--accent)', flexShrink: 0 }} />;
    }
  };

  return (
    <div
      style={{
        position: 'fixed',
        bottom: '24px',
        right: '24px',
        zIndex: 9999,
        display: 'flex',
        flexDirection: 'column',
        gap: '8px',
        maxWidth: '360px',
        width: '100%',
        pointerEvents: 'none'
      }}
    >
      {toasts.map((toast) => (
        <div
          key={toast.id}
          style={{
            pointerEvents: 'auto',
            padding: '12px 16px',
            borderRadius: '10px',
            background: 'var(--bg-card-solid)',
            border: '1px solid var(--border)',
            boxShadow: '0 8px 24px rgba(0, 0, 0, 0.25)',
            fontSize: '13px',
            color: 'var(--text-pure)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: '10px'
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            {getIcon(toast.type)}
            <span>{toast.message}</span>
          </div>
          <button
            type="button"
            onClick={() => onDismiss(toast.id)}
            style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', display: 'flex' }}
          >
            <X style={{ width: '14px', height: '14px' }} />
          </button>
        </div>
      ))}
    </div>
  );
}
