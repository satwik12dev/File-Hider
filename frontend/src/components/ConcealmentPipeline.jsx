import React, { useState } from 'react';
import { FileCode, Lock, Trash2, Database, ShieldCheck, Play, Check } from 'lucide-react';
import { cyberAudio } from '../services/cyberAudio';

export default function ConcealmentPipeline() {
  const [activeStep, setActiveStep] = useState(0);
  const [simulating, setSimulating] = useState(false);

  const steps = [
    {
      id: 1,
      title: 'Target Local File',
      desc: 'Selected confidential file on local hard drive.',
      badge: 'NTFS / C:\\',
      icon: <FileCode style={{ width: '18px', height: '18px' }} />,
      stateText: 'File Detected'
    },
    {
      id: 2,
      title: 'AES-256-GCM Encryption',
      desc: 'Transmuted into high-entropy cryptographically locked binary payload.',
      badge: '256-BIT CIPHER',
      icon: <Lock style={{ width: '18px', height: '18px' }} />,
      stateText: 'Encrypted'
    },
    {
      id: 3,
      title: 'Permanent Disk Shredding',
      desc: 'Original file unlinked and zero-byte overwritten via DoD 5220.22-M standard.',
      badge: 'ZERO RECOVERY',
      icon: <Trash2 style={{ width: '18px', height: '18px' }} />,
      stateText: 'Disk Shredded'
    },
    {
      id: 4,
      title: 'Enclave Vault Storage',
      desc: 'Binary blob securely sealed inside encrypted database storage.',
      badge: '100% CONCEALED',
      icon: <Database style={{ width: '18px', height: '18px' }} />,
      stateText: 'Hidden Forever'
    }
  ];

  const handleRunSimulation = () => {
    if (simulating) return;
    setSimulating(true);
    setActiveStep(1);
    cyberAudio.playScan();

    let step = 1;
    const interval = setInterval(() => {
      step++;
      if (step <= 4) {
        setActiveStep(step);
        cyberAudio.playBeep(500 + step * 150, 0.06);
      } else {
        clearInterval(interval);
        cyberAudio.playArm();
        setTimeout(() => {
          setSimulating(false);
        }, 1500);
      }
    }, 900);
  };

  return (
    <div className="pipeline-wrapper">
      <div className="pipeline-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <div className="cyber-terminal-dot" />
          <span className="pipeline-title">FILE CONCEALMENT & DESTRUCTION PROTOCOL</span>
        </div>
        <button
          type="button"
          className="pipeline-sim-btn"
          onClick={handleRunSimulation}
          disabled={simulating}
        >
          {simulating ? (
            <>
              <span className="hud-pulsing-dot" />
              <span>EXECUTING CIPHER...</span>
            </>
          ) : (
            <>
              <Play style={{ width: '11px', height: '11px' }} />
              <span>SIMULATE CONCEALMENT</span>
            </>
          )}
        </button>
      </div>

      <div className="pipeline-steps-grid">
        {steps.map((s, index) => {
          const isPassed = activeStep > s.id;
          const isCurrent = activeStep === s.id;

          return (
            <div
              key={s.id}
              className={`pipeline-step-card ${isCurrent ? 'active' : ''} ${isPassed ? 'completed' : ''}`}
            >
              <div className="pipeline-step-header">
                <div className="pipeline-step-num">0{s.id}</div>
                <div className="pipeline-step-badge">{s.badge}</div>
              </div>

              <div className="pipeline-icon-box">
                {isPassed ? (
                  <Check style={{ width: '18px', height: '18px', color: '#10b981' }} />
                ) : (
                  s.icon
                )}
              </div>

              <h4 className="pipeline-step-title">{s.title}</h4>
              <p className="pipeline-step-desc">{s.desc}</p>

              <div className="pipeline-step-footer">
                <span className="pipeline-step-state">
                  {isPassed ? '✓ ' + s.stateText : isCurrent ? '⚡ In Progress...' : 'Standby'}
                </span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
