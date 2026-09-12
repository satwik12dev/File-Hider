import React from 'react';
import { Shield, Trash2, KeyRound, Eye, ArrowRight, Lock, Terminal, ShieldAlert } from 'lucide-react';
import { Card } from './ui/Card';
import { Button } from './ui/Button';
import VaultScannerReticle from './VaultScannerReticle';
import ConcealmentPipeline from './ConcealmentPipeline';

export default function GuestHero({ onOpenAuth }) {
  return (
    <section className="minimal-hero">
      {/* Classified Top Pill */}
      <div className="hero-pill">
        <span className="hud-pulsing-dot" />
        <span>CLASSIFIED // ZERO-KNOWLEDGE MILITARY FILE ENCLAVE</span>
      </div>

      {/* Headline */}
      <h1 className="hero-headline">
        Secure, conceal & erase <br />
        <span className="hero-gradient-white">confidential files without trace.</span>
      </h1>

      {/* Subtext */}
      <p className="hero-subtext">
        Transform local files into encrypted binary blobs, overwrite disk sectors via DoD 5220.22-M sanitization, and restore encrypted records with zero-knowledge OTP authorization.
      </p>

      {/* Cybernetic 3D Vault with HUD Reticle & Laser Scan */}
      <div className="hero-3d-wrapper">
        <VaultScannerReticle onTriggerAction={() => onOpenAuth('login')} />
      </div>

      {/* Hero CTA Actions */}
      <div className="hero-actions">
        <Button
          variant="accent"
          size="lg"
          onClick={() => onOpenAuth('login')}
          icon={<Lock style={{ width: '16px', height: '16px' }} />}
        >
          Access Sealed Enclave
        </Button>
        <Button
          variant="outline"
          size="lg"
          onClick={() => onOpenAuth('signup')}
          icon={<ArrowRight style={{ width: '16px', height: '16px' }} />}
        >
          Initialize Operative Key
        </Button>
      </div>

      {/* Interactive Concealment Pipeline Protocol */}
      <ConcealmentPipeline />

      {/* High-Tech Security Modules Grid */}
      <div className="features-grid">
        <Card className="feature-item-card cyber-bracket-box" interactive={false}>
          <div className="feature-corner top-l">┌</div>
          <div className="feature-corner top-r">┐</div>
          <div className="feature-corner bot-l">└</div>
          <div className="feature-corner bot-r">┘</div>

          <div className="feature-icon-bubble">
            <Shield style={{ width: '18px', height: '18px' }} />
          </div>
          <div className="feature-tag-mini">PROTOCOL 01</div>
          <h3>AES-256-GCM Binary Enclave</h3>
          <p>Local files are serialized into cryptographically randomized binary payloads sealed in secure database blobs.</p>
        </Card>

        <Card className="feature-item-card cyber-bracket-box" interactive={false}>
          <div className="feature-corner top-l">┌</div>
          <div className="feature-corner top-r">┐</div>
          <div className="feature-corner bot-l">└</div>
          <div className="feature-corner bot-r">┘</div>

          <div className="feature-icon-bubble">
            <Trash2 style={{ width: '18px', height: '18px' }} />
          </div>
          <div className="feature-tag-mini">PROTOCOL 02</div>
          <h3>DoD 5220.22-M Disk Wipe</h3>
          <p>Original file paths are permanently wiped from the host file system. Disk forensics cannot recover deleted traces.</p>
        </Card>

        <Card className="feature-item-card cyber-bracket-box" interactive={false}>
          <div className="feature-corner top-l">┌</div>
          <div className="feature-corner top-r">┐</div>
          <div className="feature-corner bot-l">└</div>
          <div className="feature-corner bot-r">┘</div>

          <div className="feature-icon-bubble">
            <KeyRound style={{ width: '18px', height: '18px' }} />
          </div>
          <div className="feature-tag-mini">PROTOCOL 03</div>
          <h3>One-Time Password Biogate</h3>
          <p>Ephemeral cryptographic tokens require instant verification before decrypting or releasing encrypted blobs.</p>
        </Card>

        <Card className="feature-item-card cyber-bracket-box" interactive={false}>
          <div className="feature-corner top-l">┌</div>
          <div className="feature-corner top-r">┐</div>
          <div className="feature-corner bot-l">└</div>
          <div className="feature-corner bot-r">┘</div>

          <div className="feature-icon-bubble">
            <Eye style={{ width: '18px', height: '18px' }} />
          </div>
          <div className="feature-tag-mini">PROTOCOL 04</div>
          <h3>Zero-Trace In-Memory Preview</h3>
          <p>Directly inspect encrypted images, audio, video, and code in-memory without ever writing temporary files to storage.</p>
        </Card>
      </div>
    </section>
  );
}
