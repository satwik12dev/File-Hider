import React, { useEffect, useRef } from 'react';

const CIPHER_WORDS = [
  '0x7F9C', 'AES-256-GCM', 'SHA-512', 'ZERO_TRACE', 'DoD_5220.22-M',
  'CIPHER_LOCKED', 'ENCLAVE_ACTIVE', '01001101', 'BLOB_HASH', 'PBKDF2',
  'ENTROPY_99.9%', 'SHRED_CONFIRMED', '0x1A4F', 'NTFS_UNLINKED', 'KEY_ENCRYPTED'
];

export default function CyberSecurityBackground() {
  const canvasRef = useRef(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let animId;
    let width = (canvas.width = window.innerWidth);
    let height = (canvas.height = window.innerHeight);

    // Floating Cipher Hex Particles
    const cipherParticles = Array.from({ length: 24 }, () => ({
      x: Math.random() * width,
      y: Math.random() * height,
      text: CIPHER_WORDS[Math.floor(Math.random() * CIPHER_WORDS.length)],
      speedY: -0.3 - Math.random() * 0.4,
      opacity: 0.15 + Math.random() * 0.35,
      size: 10 + Math.floor(Math.random() * 3)
    }));

    // Interactive Constellation Cyber Defense Nodes
    const nodeCount = 38;
    const nodes = Array.from({ length: nodeCount }, () => ({
      x: Math.random() * width,
      y: Math.random() * height,
      vx: (Math.random() - 0.5) * 0.5,
      vy: (Math.random() - 0.5) * 0.5,
      radius: 1.5 + Math.random() * 2
    }));

    // Mouse tracking
    let mouse = { x: -1000, y: -1000 };
    const onMouseMove = (e) => {
      mouse.x = e.clientX;
      mouse.y = e.clientY;
    };
    window.addEventListener('mousemove', onMouseMove);

    // Radar scan angle
    let radarAngle = 0;
    let gridOffset = 0;
    let scanlineY = 0;

    const render = () => {
      ctx.clearRect(0, 0, width, height);

      // 1. Perspective Cyber Grid at the Bottom
      ctx.save();
      const gridHorizon = height * 0.62;
      const gridBottom = height;
      ctx.strokeStyle = 'rgba(255, 255, 255, 0.05)';
      ctx.lineWidth = 1;

      gridOffset = (gridOffset + 0.4) % 35;

      // Vertical perspective lines
      const centerX = width / 2;
      const lineCount = 18;
      for (let i = -lineCount; i <= lineCount; i++) {
        ctx.beginPath();
        const startX = centerX + (i * width) / (lineCount * 3.5);
        const endX = centerX + (i * width * 1.8) / lineCount;
        ctx.moveTo(startX, gridHorizon);
        ctx.lineTo(endX, gridBottom);
        ctx.stroke();
      }

      // Horizontal lines with perspective compression
      for (let y = gridHorizon; y <= gridBottom; y += 18) {
        const factor = (y - gridHorizon) / (gridBottom - gridHorizon);
        const currentY = y + (gridOffset * factor);
        if (currentY <= gridBottom) {
          ctx.beginPath();
          ctx.strokeStyle = `rgba(255, 255, 255, ${0.02 + factor * 0.08})`;
          ctx.moveTo(0, currentY);
          ctx.lineTo(width, currentY);
          ctx.stroke();
        }
      }
      ctx.restore();

      // 2. Military Background Radar Scanner (Top Right Background)
      ctx.save();
      const radarX = width * 0.82;
      const radarY = height * 0.28;
      const radarRadius = Math.min(150, width * 0.14);

      // Concentric circles
      ctx.strokeStyle = 'rgba(255, 255, 255, 0.06)';
      ctx.lineWidth = 1;
      for (let r = 35; r <= radarRadius; r += 35) {
        ctx.beginPath();
        ctx.arc(radarX, radarY, r, 0, Math.PI * 2);
        ctx.stroke();
      }

      // Crosshairs
      ctx.beginPath();
      ctx.moveTo(radarX - radarRadius, radarY);
      ctx.lineTo(radarX + radarRadius, radarY);
      ctx.moveTo(radarX, radarY - radarRadius);
      ctx.lineTo(radarX, radarY + radarRadius);
      ctx.stroke();

      // Sweeping radar beam
      radarAngle += 0.02;
      const beamEnd = {
        x: radarX + Math.cos(radarAngle) * radarRadius,
        y: radarY + Math.sin(radarAngle) * radarRadius
      };

      const grad = ctx.createRadialGradient(radarX, radarY, 0, radarX, radarY, radarRadius);
      grad.addColorStop(0, 'rgba(255, 255, 255, 0.12)');
      grad.addColorStop(1, 'rgba(255, 255, 255, 0)');

      ctx.beginPath();
      ctx.moveTo(radarX, radarY);
      ctx.arc(radarX, radarY, radarRadius, radarAngle - 0.4, radarAngle);
      ctx.closePath();
      ctx.fillStyle = grad;
      ctx.fill();

      // Radar sweep leading line
      ctx.beginPath();
      ctx.strokeStyle = 'rgba(255, 255, 255, 0.4)';
      ctx.lineWidth = 1.5;
      ctx.moveTo(radarX, radarY);
      ctx.lineTo(beamEnd.x, beamEnd.y);
      ctx.stroke();
      ctx.restore();

      // 3. Floating Cryptographic Hex/Text Stream
      ctx.save();
      ctx.font = '11px "JetBrains Mono", Consolas, monospace';
      cipherParticles.forEach((p) => {
        p.y += p.speedY;
        if (p.y < -20) {
          p.y = height + 20;
          p.x = Math.random() * width;
          p.text = CIPHER_WORDS[Math.floor(Math.random() * CIPHER_WORDS.length)];
        }

        ctx.fillStyle = `rgba(255, 255, 255, ${p.opacity})`;
        ctx.fillText(p.text, p.x, p.y);
      });
      ctx.restore();

      // 4. Interactive Defense Nodes & Links
      ctx.save();
      nodes.forEach((n, idx) => {
        n.x += n.vx;
        n.y += n.vy;

        if (n.x < 0 || n.x > width) n.vx *= -1;
        if (n.y < 0 || n.y > height) n.vy *= -1;

        // Draw node
        ctx.beginPath();
        ctx.arc(n.x, n.y, n.radius, 0, Math.PI * 2);
        ctx.fillStyle = 'rgba(255, 255, 255, 0.4)';
        ctx.fill();

        // Check distance with mouse
        const dxMouse = mouse.x - n.x;
        const dyMouse = mouse.y - n.y;
        const distMouse = Math.sqrt(dxMouse * dxMouse + dyMouse * dyMouse);
        if (distMouse < 140) {
          ctx.beginPath();
          ctx.strokeStyle = `rgba(255, 255, 255, ${0.45 * (1 - distMouse / 140)})`;
          ctx.lineWidth = 1.2;
          ctx.moveTo(n.x, n.y);
          ctx.lineTo(mouse.x, mouse.y);
          ctx.stroke();
        }

        // Link with nearby nodes
        for (let j = idx + 1; j < nodes.length; j++) {
          const n2 = nodes[j];
          const dx = n.x - n2.x;
          const dy = n.y - n2.y;
          const dist = Math.sqrt(dx * dx + dy * dy);
          if (dist < 95) {
            ctx.beginPath();
            ctx.strokeStyle = `rgba(255, 255, 255, ${0.12 * (1 - dist / 95)})`;
            ctx.lineWidth = 0.8;
            ctx.moveTo(n.x, n.y);
            ctx.lineTo(n2.x, n2.y);
            ctx.stroke();
          }
        }
      });
      ctx.restore();

      // 5. Vertical Security Laser Scanline
      scanlineY = (scanlineY + 1.6) % (height + 200);
      if (scanlineY > 0 && scanlineY < height) {
        ctx.save();
        const laserGrad = ctx.createLinearGradient(0, scanlineY - 30, 0, scanlineY + 30);
        laserGrad.addColorStop(0, 'rgba(255, 255, 255, 0)');
        laserGrad.addColorStop(0.5, 'rgba(255, 255, 255, 0.15)');
        laserGrad.addColorStop(1, 'rgba(255, 255, 255, 0)');

        ctx.fillStyle = laserGrad;
        ctx.fillRect(0, scanlineY - 30, width, 60);

        ctx.strokeStyle = 'rgba(255, 255, 255, 0.4)';
        ctx.lineWidth = 1;
        ctx.beginPath();
        ctx.moveTo(0, scanlineY);
        ctx.lineTo(width, scanlineY);
        ctx.stroke();
        ctx.restore();
      }

      animId = requestAnimationFrame(render);
    };

    render();

    const onResize = () => {
      width = canvas.width = window.innerWidth;
      height = canvas.height = window.innerHeight;
    };
    window.addEventListener('resize', onResize);

    return () => {
      cancelAnimationFrame(animId);
      window.removeEventListener('mousemove', onMouseMove);
      window.removeEventListener('resize', onResize);
    };
  }, []);

  return (
    <>
      {/* Background Interactive Cyber Canvas */}
      <canvas
        ref={canvasRef}
        style={{
          position: 'fixed',
          inset: 0,
          pointerEvents: 'none',
          zIndex: 0,
          background: '#000000'
        }}
      />

      {/* Cyber CRT Scanline Overlay Texture */}
      <div
        style={{
          position: 'fixed',
          inset: 0,
          pointerEvents: 'none',
          zIndex: 1,
          backgroundImage: 'linear-gradient(rgba(18, 16, 16, 0) 50%, rgba(0, 0, 0, 0.35) 50%)',
          backgroundSize: '100% 4px',
          opacity: 0.6
        }}
      />

      {/* Corner HUD Telemetry Reticles */}
      <div
        style={{
          position: 'fixed',
          top: '16px',
          left: '16px',
          pointerEvents: 'none',
          zIndex: 15,
          fontFamily: '"JetBrains Mono", monospace',
          fontSize: '10px',
          color: 'rgba(255, 255, 255, 0.3)',
          letterSpacing: '1px'
        }}
      >
        ┌ SEC_ZONE // ENCLAVE_ARMED
      </div>

      <div
        style={{
          position: 'fixed',
          top: '16px',
          right: '16px',
          pointerEvents: 'none',
          zIndex: 15,
          fontFamily: '"JetBrains Mono", monospace',
          fontSize: '10px',
          color: 'rgba(255, 255, 255, 0.3)',
          letterSpacing: '1px'
        }}
      >
        STATUS: DEFCON 1 ┐
      </div>

      <div
        style={{
          position: 'fixed',
          bottom: '16px',
          left: '16px',
          pointerEvents: 'none',
          zIndex: 15,
          fontFamily: '"JetBrains Mono", monospace',
          fontSize: '10px',
          color: 'rgba(255, 255, 255, 0.25)',
          letterSpacing: '1px'
        }}
      >
        └ AES_256_GCM // ZERO_FOOTPRINT
      </div>

      <div
        style={{
          position: 'fixed',
          bottom: '16px',
          right: '16px',
          pointerEvents: 'none',
          zIndex: 15,
          fontFamily: '"JetBrains Mono", monospace',
          fontSize: '10px',
          color: 'rgba(255, 255, 255, 0.25)',
          letterSpacing: '1px'
        }}
      >
        HARDWARE_SCRUB: DoD_5220 ┘
      </div>
    </>
  );
}
