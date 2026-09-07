import React from 'react';
import { Unlock, X } from 'lucide-react';
import { Button } from './ui/Button';

export default function ConfirmUnhideModal({
  file,
  isOpen,
  onClose,
  onConfirm,
  loading
}) {
  if (!isOpen || !file) return null;

  return (
    <div
      className="modal-overlay-backdrop"
      onClick={(e) => e.target === e.currentTarget && onClose()}
    >
      <div className="clean-modal-box">
        {/* Header */}
        <div className="modal-header-clean">
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <div style={{ width: '28px', height: '28px', borderRadius: '8px', background: 'var(--status-danger-subtle)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--status-danger)' }}>
              <Unlock style={{ width: '14px', height: '14px' }} />
            </div>
            <h3>Restore & Unhide</h3>
          </div>
          <button
            type="button"
            onClick={onClose}
            style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', display: 'flex' }}
          >
            <X style={{ width: '18px', height: '18px' }} />
          </button>
        </div>

        {/* Body */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <div style={{ padding: '10px 14px', borderRadius: '8px', background: 'var(--bg-subtle)', border: '1px solid var(--border)' }}>
            <strong style={{ fontSize: '13px', color: 'var(--text-pure)', display: 'block' }}>{file.fileName}</strong>
            <span style={{ fontSize: '11px', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>{file.path}</span>
          </div>

          <p style={{ fontSize: '13px', color: 'var(--text-secondary)', lineHeight: '1.5' }}>
            Are you sure you want to restore this file? The encrypted record will be permanently purged from your vault.
          </p>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px', marginTop: '6px' }}>
            <Button variant="outline" size="md" onClick={onClose} disabled={loading}>
              Cancel
            </Button>
            <Button
              variant="accent"
              size="md"
              onClick={onConfirm}
              disabled={loading}
              icon={<Unlock style={{ width: '14px', height: '14px' }} />}
            >
              {loading ? 'Restoring...' : 'Confirm Restore'}
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
