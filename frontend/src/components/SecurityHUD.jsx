import React, { useState, useEffect } from 'react';
import { ShieldAlert, Cpu, HardDrive, RefreshCw, CheckCircle2, Lock } from 'lucide-react';
import { cyberAudio } from '../services/cyberAudio';

export default function SecurityHUD() {
  const [rollingHash, setRollingHash] = useState('e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855');
  const [auditing, setAuditing] = useState(false);
  const [auditStep, setAuditStep] = useState(null);

  useEffect(() => {
    const chars = '0123456789abcdef';
    const interval = setInterval(() => {
      let hash = '';
      for (let i = 0; i < 48; i++) {
        hash += chars[Math.floor(Math.random() * chars.length)];
      }
      setRollingHash(hash);
    }, 180);
    return () => clearInterval(interval);
  }, []);

  const handleRunAudit = () => {
    if (auditing) return;
    setAuditing(true);
    cyberAudio.playArm();

    const steps = [
      'Auditing Cryptographic Blob Integrity...',
      'Verifying DoD 5220.22-M Disk Wipe Sanitizer...',
      'Testing PBKDF2 Master Key Salt & Entropy...',
      'Validating Zero-Trace Enclave Boundaries...',
      'SYSTEM SEALED // 100% SECURE'
    ];

    steps.forEach((stepText, idx) => {
      setTimeout(() => {
        setAuditStep(stepText);
        cyberAudio.playBeep(700 + idx * 120, 0.04);
        if (idx === steps.length - 1) {
          setTimeout(() => {
            setAuditing(false);
            setAuditStep(null);
          }, 2000);
        }
      }, idx * 600);
    });
  };

  return (
    <div className="security-hud-bar">
      {/* Left: Security Status Badge */}
      <div className="hud-metric-group">
        <div className="hud-badge-defcon">
          <span className="hud-pulsing-dot" />
          <span>ENCLAVE: ARMED</span>
        </div>
        <div className="hud-divider" />
        <div className="hud-item">
          <Lock style={{ width: '12px', height: '12px', color: '#ffffff' }} />
          <span>CIPHER: <strong>AES-256-GCM</strong></span>
        </div>
        <div className="hud-divider" />
        <div className="hud-item">
          <HardDrive style={{ width: '12px', height: '12px', color: '#ffffff' }} />
          <span>SHREDDER: <strong>DoD 5220.22-M</strong></span>
        </div>
      </div>

      {/* Middle: Live Cryptographic Hash Stream */}
      <div className="hud-hash-stream" title="Real-time Cryptographic Entropy & Hash Stream">
        <span className="hud-hash-label">SHA-256 HASH:</span>
        <span className="hud-hash-code">{rollingHash}</span>
      </div>

      {/* Right: Security Audit Trigger */}
      <div className="hud-action-wrap">
        {auditing ? (
          <div className="hud-auditing-badge">
            <RefreshCw className="hud-spin" style={{ width: '12px', height: '12px' }} />
            <span>{auditStep}</span>
          </div>
        ) : (
          <button
            type="button"
            className="hud-audit-btn"
            onClick={handleRunAudit}
            title="Perform hardware cryptographic enclave integrity audit"
          >
            <ShieldAlert style={{ width: '12px', height: '12px' }} />
            <span>RUN AUDIT</span>
          </button>
        )}
      </div>
    </div>
  );
}
