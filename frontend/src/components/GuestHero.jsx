import React from 'react';
import { Shield, Trash2, KeyRound, Eye, ArrowRight, Lock } from 'lucide-react';
import { Card } from './ui/Card';
import { Button } from './ui/Button';
import ThreeVaultScene from './ThreeVaultScene';

export default function GuestHero({ onOpenAuth }) {
  return (
    <section className="minimal-hero">
      {/* Top pill */}
      <div className="hero-pill">
        <span style={{ color: 'var(--accent)' }}>✦</span>
        <span>Zero-Knowledge File Security Enclave</span>
      </div>

      {/* Headline */}
      <h1 className="hero-headline">
        Secure, conceal & encrypt <br />
        <span style={{ color: 'var(--accent)' }}>your confidential files.</span>
      </h1>

      {/* Subtext */}
      <p className="hero-subtext">
        Store sensitive files directly into encrypted database blobs and erase local disk traces. Inspect and restore your assets anytime with verified one-time authentication.
      </p>

      {/* 3D Minimal Centerpiece */}
      <div className="hero-3d-wrapper">
        <ThreeVaultScene onTriggerAction={() => onOpenAuth('login')} />
      </div>

      {/* Actions */}
      <div className="hero-actions">
        <Button
          variant="accent"
          size="lg"
          onClick={() => onOpenAuth('login')}
          icon={<Lock style={{ width: '16px', height: '16px' }} />}
        >
          Open Secure Vault
        </Button>
        <Button
          variant="outline"
          size="lg"
          onClick={() => onOpenAuth('signup')}
          icon={<ArrowRight style={{ width: '16px', height: '16px' }} />}
        >
          Create Operative
        </Button>
      </div>

      {/* Feature Grid */}
      <div className="features-grid">
        <Card className="feature-item-card" interactive={false}>
          <div className="feature-icon-bubble">
            <Shield style={{ width: '18px', height: '18px' }} />
          </div>
          <h3>AES-256 Storage</h3>
          <p>Files are converted into binary encrypted blobs stored safely in database storage.</p>
        </Card>

        <Card className="feature-item-card" interactive={false}>
          <div className="feature-icon-bubble">
            <Trash2 style={{ width: '18px', height: '18px' }} />
          </div>
          <h3>Local Disk Wiping</h3>
          <p>Original source files are permanently unlinked and purged from your local hard drive.</p>
        </Card>

        <Card className="feature-item-card" interactive={false}>
          <div className="feature-icon-bubble">
            <KeyRound style={{ width: '18px', height: '18px' }} />
          </div>
          <h3>OTP Verification Gate</h3>
          <p>Strict one-time password security ensures only authorized operatives can unlock files.</p>
        </Card>

        <Card className="feature-item-card" interactive={false}>
          <div className="feature-icon-bubble">
            <Eye style={{ width: '18px', height: '18px' }} />
          </div>
          <h3>In-Vault Preview</h3>
          <p>Inspect encrypted documents, images, and code directly before decrypting to disk.</p>
        </Card>
      </div>
    </section>
  );
}
