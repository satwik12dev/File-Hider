import React from 'react';
import ThreeVaultScene from './ThreeVaultScene';

export default function VaultScannerReticle({ onTriggerAction }) {
  return (
    <div className="vault-scanner-container">
      {/* Corner Cyber Brackets */}
      <span className="reticle-bracket top-left">┌</span>
      <span className="reticle-bracket top-right">┐</span>
      <span className="reticle-bracket bottom-left">└</span>
      <span className="reticle-bracket bottom-right">┘</span>

      {/* Telemetry Tags */}
      <div className="reticle-tag top-left-tag">
        <span className="reticle-dot" />
        <span>CYPHER_CORE // ONLINE</span>
      </div>

      <div className="reticle-tag top-right-tag">
        <span>BIO_HASH: 0x9FA2</span>
      </div>

      <div className="reticle-tag bottom-left-tag">
        <span>LOCK_ENGAGED: TRUE</span>
      </div>

      <div className="reticle-tag bottom-right-tag">
        <span>ZERO_FOOTPRINT</span>
      </div>

      {/* Rotating Cyber Reticle Rings */}
      <div className="reticle-ring outer-ring" />
      <div className="reticle-ring inner-ring" />

      {/* Laser Scanline */}
      <div className="reticle-laser-scanner" />

      {/* The 3D Vault */}
      <div className="vault-scene-holder">
        <ThreeVaultScene onTriggerAction={onTriggerAction} />
      </div>
    </div>
  );
}
